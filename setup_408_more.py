import requests, json, random

BASE = 'http://localhost:8080'
r = requests.post(f'{BASE}/user/login', json={'username':'2303031555','password':'123456'})
T = r.json()['data']['token']
H = {'Content-Type':'application/json','Authorization':f'Bearer {T}'}

# 获取所有题目按科目分组
r = requests.get(f'{BASE}/question/list', headers=H)
all_q = r.json()['data']
cats = {}
for q in all_q:
    cats.setdefault(q['category'], []).append(q)
print('各科目题数:', {c: len(cats[c]) for c in cats})

# 答案映射
amap = {str(q['id']): q for q in all_q}

# 创建3份试卷(每份每科随机抽5题,共20题,每题5分=100分),允许跨卷复用
new_papers = []
nums = ['八','九','十']
random.seed(888)
for i in range(3):
    qs = []
    for cat in ['数据结构','计算机组成原理','操作系统','计算机网络']:
        pool = cats.get(cat, [])
        qs.extend(random.sample(pool, 5))
    data = {
        'title': f'408全真模拟卷{nums[i]}',
        'description': f'408计算机学科专业基础综合模拟卷{8+i}',
        'duration': 120,
        'startTime': '2026-01-01 00:00:00', 'endTime': '2099-12-31 23:59:59',
        'questions': [{'questionId': str(q['id']), 'score': 5} for q in qs]
    }
    r = requests.post(f'{BASE}/paper/create', headers=H, json=data)
    if r.json()['code'] == 200:
        r2 = requests.get(f'{BASE}/paper/list', headers=H)
        papers = sorted(r2.json()['data'], key=lambda x: str(x['id']), reverse=True)
        pid = papers[0]['id']
        requests.put(f'{BASE}/paper/publish/{pid}', headers=H)
        new_papers.append(pid)
        print(f'  [已发布] 408全真模拟卷{nums[i]} id={pid} 20题100分')

# 学生答这3份卷(故意错40%)
def gen_wrong(qid, correct):
    info = amap.get(str(qid), {})
    t = info.get('type','')
    if t == 'single_choice':
        opts = [o for o in ['A','B','C','D'] if info.get('option'+o)]
        wrong = [o for o in opts if o != correct]
        return random.choice(wrong) if wrong else 'A'
    elif t == 'multi_choice':
        opts = [o for o in ['A','B','C','D'] if info.get('option'+o)]
        cset = set(correct.split(','))
        wrong = [o for o in opts if o not in cset]
        if wrong: return random.choice(wrong)
        return correct
    elif t == 'true_false':
        return 'B' if correct == 'A' else 'A'
    elif t == 'fill_blank':
        return '错误答案'
    return 'wrong'

students = ['23030315','student1','student2','student3','student4','student5','student6',
            'student7','student8','student9','student10','student11']
cnt = 0
for uname in students:
    r = requests.post(f'{BASE}/user/login', json={'username':uname,'password':'123456'})
    if r.json().get('code') != 200: continue
    ST = r.json()['data']['token']
    SH = {'Content-Type':'application/json','Authorization':f'Bearer {ST}'}
    for pid in new_papers:
        r = requests.post(f'{BASE}/exam/start', headers=SH, json={'paperId': str(pid)})
        if r.json().get('code') != 200: continue
        data = r.json()['data']
        eid = data['examRecordId']
        for q in data['questions']:
            qid = str(q['id'])
            correct = amap.get(qid, {}).get('answer', '')
            if not correct: continue
            ans = gen_wrong(qid, correct) if random.random() < 0.4 else correct
            requests.post(f'{BASE}/exam/answer', headers=SH, json={
                'examRecordId': str(eid), 'questionId': qid, 'userAnswer': ans})
        r = requests.post(f'{BASE}/exam/submit/{eid}', headers=SH)
        if r.json().get('code') == 200: cnt += 1
    print(f'  {uname}: 完成 {len(new_papers)} 份')

print(f'\n补建试卷: {len(new_papers)}份')
print(f'补答题数: {cnt}次')
