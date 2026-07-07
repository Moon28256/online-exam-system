import requests, json, random, time

BASE = 'http://localhost:8080'

# 登录教师
r = requests.post(f'{BASE}/user/login', json={'username':'teacher1','password':'123456'})
T = r.json()['data']['token']
TH = {'Content-Type':'application/json','Authorization':f'Bearer {T}'}

# 获取所有题目，按类别分组
r = requests.get(f'{BASE}/question/list', headers=TH)
all_q = r.json()['data']
cats = {}
for q in all_q:
    cats.setdefault(q['category'], []).append(q)
print(f'Total questions: {len(all_q)}')
for c, qs in sorted(cats.items()):
    print(f'  {c}: {len(qs)} questions')

# 获取全部试卷ID
r = requests.get(f'{BASE}/paper/list', headers=TH)
existing = r.json()['data']

# 发布所有未发布的试卷
for p in existing:
    if p['status'] == 'draft':
        requests.put(f'{BASE}/paper/publish/{p["id"]}', headers=TH)
        print(f'  Published: {p["title"]}')

# 创建5份新试卷
new_papers = [
    ('408综合卷一', '数据结构+计组+操作系统+计网全科模拟',
     cats.get('数据结构',[])[:3] + cats.get('计算机组成原理',[])[:3] + cats.get('操作系统',[])[:2] + cats.get('计算机网络',[])[:2]),
    ('408综合卷二', '408计算机学科综合模拟测试',
     cats.get('数据结构',[])[3:6] + cats.get('计算机组成原理',[])[3:6] + cats.get('操作系统',[])[2:4] + cats.get('计算机网络',[])[2:4]),
    ('408综合卷三', '四大科目均衡测试',
     cats.get('数据结构',[])[:4] + cats.get('计算机组成原理',[])[:3] + cats.get('操作系统',[])[:3]),
    ('408计组与OS专项', '计算机组成原理与操作系统深入练习',
     cats.get('计算机组成原理',[])[:5] + cats.get('操作系统',[])[:5]),
    ('408数据结构与计网专项', '数据结构与计算机网络重点',
     cats.get('数据结构',[])[:5] + cats.get('计算机网络',[])[:5]),
]

all_paper_ids = [p['id'] for p in existing]

for title, desc, qs in new_papers:
    if len(qs) < 2:
        print(f'  SKIP {title}: not enough questions')
        continue
    data = {
        'title': title, 'description': desc, 'duration': 120,
        'startTime': '2026-01-01 00:00:00', 'endTime': '2099-12-31 23:59:59',
        'questions': [{'questionId': str(q['id']), 'score': 5} for q in qs]
    }
    r = requests.post(f'{BASE}/paper/create', headers=TH, json=data)
    if r.json()['code'] == 200:
        # 找到新创建的试卷
        r2 = requests.get(f'{BASE}/paper/list', headers=TH)
        papers = sorted(r2.json()['data'], key=lambda x: str(x['id']), reverse=True)
        pid = papers[0]['id']
        # 发布
        requests.put(f'{BASE}/paper/publish/{pid}', headers=TH)
        all_paper_ids.append(pid)
        print(f'  [PUBLISHED] {title} (id={pid}) {len(qs)}qs {len(qs)*5}pts')

print(f'\nAll papers: {len(all_paper_ids)}')

# 构建正确答案映射（从题库获取）
answer_map = {}
for q in all_q:
    answer_map[str(q['id'])] = {
        'answer': q.get('answer',''),
        'type': q.get('type',''),
        'optionA': q.get('optionA',''),
        'optionB': q.get('optionB',''),
        'optionC': q.get('optionC',''),
        'optionD': q.get('optionD',''),
    }

def get_correct(qid):
    return answer_map.get(str(qid), {}).get('answer', '')

def get_type(qid):
    return answer_map.get(str(qid), {}).get('type', '')

def generate_wrong(qid, correct):
    info = answer_map.get(str(qid), {})
    t = info.get('type','')
    if t == 'single_choice':
        opts = [o for o in ['A','B','C','D'] if info.get('option'+o)]
        wrong = [o for o in opts if o != correct]
        return random.choice(wrong) if wrong else 'A'
    elif t == 'multi_choice':
        opts = [o for o in ['A','B','C','D'] if info.get('option'+o)]
        correct_set = set(correct.split(','))
        wrong = [o for o in opts if o not in correct_set]
        if wrong: return random.choice(wrong)
        return correct
    elif t == 'true_false':
        return 'B' if correct == 'A' else 'A'
    elif t == 'fill_blank':
        return '错误的答案'
    else:
        return 'wrong'

# ===== 11名学生模拟答题 =====
students = [
    ('student1','123456'),('student2','123456'),('student3','123456'),
    ('student4','123456'),('student5','123456'),('student6','123456'),
    ('student7','123456'),('student8','123456'),('student9','123456'),
    ('student10','123456'),('student11','123456'),
]

# 故意答错策略：每份试卷随机错30%-50%的题
random.seed(42)
exam_count = 0

for uname, pwd in students:
    # 学生登录
    r = requests.post(f'{BASE}/user/login', json={'username':uname,'password':pwd})
    ST = r.json()['data']['token']
    SH = {'Content-Type':'application/json','Authorization':f'Bearer {ST}'}

    for pid in all_paper_ids:
        # 开始考试
        r = requests.post(f'{BASE}/exam/start', headers=SH, json={'paperId': str(pid)})
        if r.json().get('code') != 200:
            continue
        data = r.json()['data']
        eid = data['examRecordId']
        questions = data['questions']

        # 对每道题作答 — 故意随机错一部分
        for q in questions:
            qid = str(q['id'])
            correct = get_correct(qid)
            if not correct: continue

            # ~40% 概率答错
            if random.random() < 0.4:
                ans = generate_wrong(qid, correct)
            else:
                ans = correct

            requests.post(f'{BASE}/exam/answer', headers=SH, json={
                'examRecordId': str(eid),
                'questionId': qid,
                'userAnswer': ans
            })

        # 交卷
        r = requests.post(f'{BASE}/exam/submit/{eid}', headers=SH)
        if r.json().get('code') == 200:
            d = r.json()['data']
            exam_count += 1
            # 只每5个考试打印一次
            if exam_count % 10 == 0:
                print(f'  ...{exam_count} exams completed')

    print(f'  {uname}: done all papers')

print(f'\nTotal exams submitted: {exam_count}')
print('Done!')
