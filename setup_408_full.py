import requests, json, random

BASE = 'http://localhost:8080'

# 登录 2303031555
r = requests.post(f'{BASE}/user/login', json={'username':'2303031555','password':'123456'})
T = r.json()['data']['token']
H = {'Content-Type':'application/json','Authorization':f'Bearer {T}'}
print(f'登录 2303031555 成功')

# ===== 100道408题目 =====
DS = [  # 数据结构
    {'type':'single_choice','content':'栈的特点是？','optionA':'先进先出','optionB':'后进先出','optionC':'随机存取','optionD':'索引存取','answer':'B'},
    {'type':'single_choice','content':'队列的特点是？','optionA':'先进先出','optionB':'后进先出','optionC':'随机存取','optionD':'散列存取','answer':'A'},
    {'type':'single_choice','content':'单链表的存储结构特点是？','optionA':'随机存取','optionB':'顺序存取','optionC':'索引存取','optionD':'散列存取','answer':'B'},
    {'type':'single_choice','content':'二分查找要求线性表？','optionA':'链表存储','optionB':'顺序存储且有序','optionC':'散列存储','optionD':'索引存储','answer':'B'},
    {'type':'single_choice','content':'二叉树第i层(i从1开始)最多有多少节点？','optionA':'2^i','optionB':'2^(i-1)','optionC':'i','optionD':'2i','answer':'B'},
    {'type':'single_choice','content':'深度为k的二叉树最多有多少节点？','optionA':'2^k','optionB':'2^k-1','optionC':'k','optionD':'2k-1','answer':'B'},
    {'type':'single_choice','content':'n个节点的完全二叉树深度为？','optionA':'⌊log2n⌋+1','optionB':'log2n','optionC':'n/2','optionD':'n','answer':'A'},
    {'type':'single_choice','content':'任何二叉树中叶子数n0与度为2节点数n2的关系？','optionA':'n0=n2+1','optionB':'n0=n2-1','optionC':'n0=2n2','optionD':'n0=n2','answer':'A'},
    {'type':'single_choice','content':'快速排序的平均时间复杂度是？','optionA':'O(n)','optionB':'O(nlogn)','optionC':'O(n^2)','optionD':'O(logn)','answer':'B'},
    {'type':'single_choice','content':'归并排序的时间复杂度是？','optionA':'O(n)','optionB':'O(nlogn)','optionC':'O(n^2)','optionD':'O(1)','answer':'B'},
    {'type':'single_choice','content':'堆排序的平均时间复杂度是？','optionA':'O(n)','optionB':'O(nlogn)','optionC':'O(n^2)','optionD':'O(n^1.5)','answer':'B'},
    {'type':'single_choice','content':'Dijkstra最短路径算法基于？','optionA':'贪心','optionB':'动态规划','optionC':'回溯','optionD':'分治','answer':'A'},
    {'type':'single_choice','content':'拓扑排序可用于判断有向图是否有？','optionA':'环','optionB':'桥','optionC':'割点','optionD':'连通分量','answer':'A'},
    {'type':'multi_choice','content':'以下哪些是稳定排序算法？','optionA':'冒泡排序','optionB':'快速排序','optionC':'归并排序','optionD':'插入排序','answer':'A,C,D'},
    {'type':'multi_choice','content':'以下哪些是线性结构？','optionA':'栈','optionB':'队列','optionC':'树','optionD':'图','answer':'A,B'},
    {'type':'multi_choice','content':'以下哪些是图的遍历算法？','optionA':'深度优先DFS','optionB':'广度优先BFS','optionC':'Dijkstra','optionD':'Prim','answer':'A,B'},
    {'type':'multi_choice','content':'以下哪些是哈希冲突处理方法？','optionA':'线性探测','optionB':'链地址法','optionC':'二次探测','optionD':'折半查找','answer':'A,B,C'},
    {'type':'true_false','content':'栈是一种后进先出的线性表。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'队列是一种先进先出的线性表。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'二叉搜索树的中序遍历得到有序序列。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'哈希表的查找效率与冲突处理方法无关。','optionA':'正确','optionB':'错误','answer':'B'},
    {'type':'true_false','content':'哈夫曼树中不存在度为1的节点。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'fill_blank','content':'一个栈的入栈序列是1,2,3,4,则不可能的出栈序列中,4在第一个位置时,出栈序列是______。','answer':'4,3,2,1'},
    {'type':'fill_blank','content':'循环队列中,队空的条件是front等于______。','answer':'rear'},
    {'type':'fill_blank','content':'n个叶子节点的哈夫曼树共有______个节点。','answer':'2n-1'},
]

CO = [  # 计算机组成原理
    {'type':'single_choice','content':'冯·诺依曼计算机的基本特点包括？','optionA':'存储程序','optionB':'流水线','optionC':'并行处理','optionD':'分布式','answer':'A'},
    {'type':'single_choice','content':'CPU主要由哪两部分组成？','optionA':'运算器和控制器','optionB':'内存和外存','optionC':'输入和输出','optionD':'总线和接口','answer':'A'},
    {'type':'single_choice','content':'存储器分级体系是为解决？','optionA':'速度容量成本矛盾','optionB':'容量不足','optionC':'速度不足','optionD':'成本过高','answer':'A'},
    {'type':'single_choice','content':'Cache的工作原理基于？','optionA':'程序局部性原理','optionB':'对换技术','optionC':'虚拟存储','optionD':'直接映射','answer':'A'},
    {'type':'single_choice','content':'以下不是Cache地址映射方式的是？','optionA':'直接映射','optionB':'全相联映射','optionC':'组相联映射','optionD':'哈希映射','answer':'D'},
    {'type':'single_choice','content':'一个字节等于多少位？','optionA':'4','optionB':'8','optionC':'16','optionD':'32','answer':'B'},
    {'type':'single_choice','content':'浮点数表示中决定精度的是？','optionA':'阶码','optionB':'尾数','optionC':'符号位','optionD':'基数','answer':'B'},
    {'type':'single_choice','content':'IEEE754单精度浮点数共多少位？','optionA':'16','optionB':'32','optionC':'64','optionD':'128','answer':'B'},
    {'type':'single_choice','content':'补码表示中,零的表示是？','optionA':'唯一的','optionB':'不唯一','optionC':'两种','optionD':'不存在','answer':'A'},
    {'type':'single_choice','content':'算术左移相当于？','optionA':'乘以2','optionB':'除以2','optionC':'加1','optionD':'减1','answer':'A'},
    {'type':'single_choice','content':'RISC指令系统的特点是？','optionA':'指令少且简单','optionB':'指令多且复杂','optionC':'指令长度可变','optionD':'寻址方式多','answer':'A'},
    {'type':'single_choice','content':'程序计数器PC存放的是？','optionA':'当前指令','optionB':'下一条指令地址','optionC':'操作数','optionD':'运算结果','answer':'B'},
    {'type':'single_choice','content':'指令周期是指？','optionA':'取指与执行的时间','optionB':'一个时钟周期','optionC':'一个总线周期','optionD':'一个机器周期','answer':'A'},
    {'type':'single_choice','content':'DMA方式适用于？','optionA':'高速外设','optionB':'低速外设','optionC':'运算器','optionD':'控制器','answer':'A'},
    {'type':'single_choice','content':'中断优先级最高通常是？','optionA':'电源故障','optionB':'打印机','optionC':'键盘','optionD':'鼠标','answer':'A'},
    {'type':'multi_choice','content':'以下哪些是总线类型？','optionA':'数据总线','optionB':'地址总线','optionC':'控制总线','optionD':'电源总线','answer':'A,B,C'},
    {'type':'multi_choice','content':'以下哪些是寻址方式？','optionA':'立即寻址','optionB':'直接寻址','optionC':'间接寻址','optionD':'寄存器寻址','answer':'A,B,C,D'},
    {'type':'multi_choice','content':'以下哪些属于控制器组成？','optionA':'程序计数器PC','optionB':'指令寄存器IR','optionC':'算术逻辑单元ALU','optionD':'控制单元CU','answer':'A,B,D'},
    {'type':'multi_choice','content':'以下哪些是Cache替换算法？','optionA':'FIFO','optionB':'LRU','optionC':'LFU','optionD':'OPT','answer':'A,B,C,D'},
    {'type':'true_false','content':'Cache是为了解决CPU与主存速度不匹配的问题。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'虚拟存储器是为了解决主存容量不足的问题。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'补码中零有两种表示。','optionA':'正确','optionB':'错误','answer':'B'},
    {'type':'true_false','content':'流水线技术能提高单条指令的执行速度。','optionA':'正确','optionB':'错误','answer':'B'},
    {'type':'fill_blank','content':'1个字节等于8个______。','answer':'位'},
    {'type':'fill_blank','content':'存放当前正在执行指令的寄存器是______。','answer':'IR'},
]

OS = [  # 操作系统
    {'type':'single_choice','content':'操作系统是最基本的？','optionA':'系统软件','optionB':'应用软件','optionC':'编译程序','optionD':'工具软件','answer':'A'},
    {'type':'single_choice','content':'以下不是进程基本特征的是？','optionA':'动态性','optionB':'并发性','optionC':'独立性','optionD':'永恒性','answer':'D'},
    {'type':'single_choice','content':'以下不是进程三种基本状态的是？','optionA':'就绪','optionB':'运行','optionC':'阻塞','optionD':'挂起','answer':'D'},
    {'type':'single_choice','content':'临界区是指？','optionA':'并发进程访问共享变量的代码段','optionB':'一段数据','optionC':'一个进程','optionD':'一个资源','answer':'A'},
    {'type':'single_choice','content':'信号量初值为1可用于实现？','optionA':'互斥','optionB':'同步','optionC':'计数','optionD':'死锁','answer':'A'},
    {'type':'single_choice','content':'以下不是死锁必要条件的是？','optionA':'互斥','optionB':'占有并等待','optionC':'不剥夺','optionD':'共享','answer':'D'},
    {'type':'single_choice','content':'银行家算法用于？','optionA':'死锁避免','optionB':'死锁预防','optionC':'死锁检测','optionD':'死锁恢复','answer':'A'},
    {'type':'single_choice','content':'页式存储管理中,逻辑地址由？','optionA':'页号和页内偏移','optionB':'段号和偏移','optionC':'段号和页号','optionD':'基址和偏移','answer':'A'},
    {'type':'single_choice','content':'LRU页面置换算法淘汰？','optionA':'最久未使用的页面','optionB':'最先进入的页面','optionC':'使用最少的页面','optionD':'随机页面','answer':'A'},
    {'type':'single_choice','content':'SSTF磁盘调度算法是？','optionA':'最短寻道时间优先','optionB':'先来先服务','optionC':'扫描','optionD':'循环扫描','answer':'A'},
    {'type':'single_choice','content':'文件系统的主要目的是？','optionA':'实现按名存取','optionB':'加密','optionC':'压缩','optionD':'备份','answer':'A'},
    {'type':'single_choice','content':'虚拟存储器的最大容量受限于？','optionA':'地址空间','optionB':'内存容量','optionC':'磁盘容量','optionD':'CPU','answer':'A'},
    {'type':'single_choice','content':'分时系统的关键是？','optionA':'时间片','optionB':'批处理','optionC':'实时响应','optionD':'交互','answer':'A'},
    {'type':'single_choice','content':'磁盘物理地址由？','optionA':'柱面号磁头号扇区号','optionB':'页号和偏移','optionC':'段号','optionD':'索引','answer':'A'},
    {'type':'multi_choice','content':'以下哪些是进程调度算法？','optionA':'先来先服务FCFS','optionB':'短作业优先SJF','optionC':'时间片轮转RR','optionD':'优先级调度','answer':'A,B,C,D'},
    {'type':'multi_choice','content':'以下哪些是页面置换算法？','optionA':'最佳置换OPT','optionB':'先进先出FIFO','optionC':'最近最少使用LRU','optionD':'最不常用LFU','answer':'A,B,C,D'},
    {'type':'multi_choice','content':'以下哪些是进程间通信方式？','optionA':'管道','optionB':'消息队列','optionC':'共享内存','optionD':'信号量','answer':'A,B,C,D'},
    {'type':'multi_choice','content':'死锁的四个必要条件是？','optionA':'互斥','optionB':'占有并等待','optionC':'不剥夺','optionD':'循环等待','answer':'A,B,C,D'},
    {'type':'true_false','content':'SPOOLing技术可将独占设备改造为共享设备。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'系统处于安全状态就一定不会发生死锁。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'虚拟存储器扩大了内存的逻辑容量。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'进程是资源分配的基本单位。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'fill_blank','content':'进程的三种基本状态是就绪、运行和______。','answer':'阻塞'},
    {'type':'fill_blank','content':'分页存储管理中逻辑地址分为页号和______。','answer':'页内偏移'},
    {'type':'fill_blank','content':'产生死锁的四个必要条件是互斥、占有等待、不剥夺和______。','answer':'循环等待'},
]

CN = [  # 计算机网络
    {'type':'single_choice','content':'OSI参考模型共有几层？','optionA':'4','optionB':'5','optionC':'7','optionD':'6','answer':'C'},
    {'type':'single_choice','content':'TCP/IP模型共有几层？','optionA':'4','optionB':'5','optionC':'7','optionD':'6','answer':'A'},
    {'type':'single_choice','content':'物理层的数据传输单位是？','optionA':'比特','optionB':'帧','optionC':'报文','optionD':'分组','answer':'A'},
    {'type':'single_choice','content':'数据链路层的数据单位是？','optionA':'比特','optionB':'帧','optionC':'报文','optionD':'分组','answer':'B'},
    {'type':'single_choice','content':'网络层的数据单位是？','optionA':'比特','optionB':'帧','optionC':'分组','optionD':'段','answer':'C'},
    {'type':'single_choice','content':'传输层的数据单位是？','optionA':'比特','optionB':'帧','optionC':'报文','optionD':'段','answer':'D'},
    {'type':'single_choice','content':'IP协议工作在OSI的？','optionA':'网络层','optionB':'数据链路层','optionC':'传输层','optionD':'应用层','answer':'A'},
    {'type':'single_choice','content':'TCP协议工作在？','optionA':'应用层','optionB':'传输层','optionC':'网络层','optionD':'链路层','answer':'B'},
    {'type':'single_choice','content':'HTTP协议默认端口是？','optionA':'21','optionB':'22','optionC':'80','optionD':'443','answer':'C'},
    {'type':'single_choice','content':'HTTPS协议默认端口是？','optionA':'80','optionB':'443','optionC':'21','optionD':'22','answer':'B'},
    {'type':'single_choice','content':'DNS协议默认端口是？','optionA':'53','optionB':'80','optionC':'21','optionD':'443','answer':'A'},
    {'type':'single_choice','content':'ARP协议的作用是？','optionA':'IP转MAC','optionB':'MAC转IP','optionC':'域名转IP','optionD':'IP转域名','answer':'A'},
    {'type':'single_choice','content':'TCP建立连接需要几次握手？','optionA':'2','optionB':'3','optionC':'4','optionD':'1','answer':'B'},
    {'type':'single_choice','content':'TCP释放连接需要几次挥手？','optionA':'2','optionB':'3','optionC':'4','optionD':'1','answer':'C'},
    {'type':'single_choice','content':'以下面向连接的协议是？','optionA':'TCP','optionB':'UDP','optionC':'IP','optionD':'ICMP','answer':'A'},
    {'type':'multi_choice','content':'以下哪些是网络层协议？','optionA':'IP','optionB':'ICMP','optionC':'TCP','optionD':'UDP','answer':'A,B'},
    {'type':'multi_choice','content':'以下哪些是传输层协议？','optionA':'TCP','optionB':'UDP','optionC':'IP','optionD':'HTTP','answer':'A,B'},
    {'type':'multi_choice','content':'以下哪些是应用层协议？','optionA':'HTTP','optionB':'FTP','optionC':'SMTP','optionD':'DNS','answer':'A,B,C,D'},
    {'type':'multi_choice','content':'以下哪些是私有IP地址段？','optionA':'10.x.x.x','optionB':'172.16.x.x','optionC':'192.168.x.x','optionD':'8.8.8.8','answer':'A,B,C'},
    {'type':'true_false','content':'TCP提供可靠的数据传输服务。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'true_false','content':'UDP提供可靠的数据传输服务。','optionA':'正确','optionB':'错误','answer':'B'},
    {'type':'true_false','content':'DNS主要使用TCP进行域名解析。','optionA':'正确','optionB':'错误','answer':'B'},
    {'type':'true_false','content':'TCP三次握手可以防止已失效的连接请求到达服务器。','optionA':'正确','optionB':'错误','answer':'A'},
    {'type':'fill_blank','content':'IPv6地址长度为______位。','answer':'128'},
    {'type':'fill_blank','content':'TCP建立连接的过程称为______握手。','answer':'三'},
]

subjects = [('数据结构',DS), ('计算机组成原理',CO), ('操作系统',OS), ('计算机网络',CN)]
all_qlist = []
for cat, qs in subjects:
    for q in qs:
        q['category'] = cat
        q['difficulty'] = 'medium'
        q['score'] = 2
        all_qlist.append(q)

print(f'共 {len(all_qlist)} 道题')
# 添加题目
ok = 0
for i, q in enumerate(all_qlist):
    r = requests.post(f'{BASE}/question/add', headers=H, json=q)
    if r.json()['code'] == 200: ok += 1
    else: print(f'  FAIL [{i+1}] {q["category"]} {q["content"][:20]}: {r.json().get("message")}')
print(f'添加成功 {ok}/{len(all_qlist)}')

# 获取所有题目(本教师),按类别分组
r = requests.get(f'{BASE}/question/list', headers=H)
all_q = r.json()['data']
cats = {}
for q in all_q:
    cats.setdefault(q['category'], []).append(q)
# 只取本次新增的100题(按created_time最新排序,取后100)
for c in cats: cats[c].reverse()  # 新的在前
print('各科目题数:', {c: len(cats[c]) for c in cats})

# 创建10份试卷,每份20题(每科5题),每题5分,满分100
paper_ids = []
for pidx in range(10):
    qs = []
    for cat in ['数据结构','计算机组成原理','操作系统','计算机网络']:
        clist = cats.get(cat, [])
        qs.extend(clist[pidx*5:(pidx+1)*5])
    if len(qs) < 20:
        print(f'  试卷{pidx+1}题数不足:{len(qs)},跳过')
        continue
    data = {
        'title': f'408全真模拟卷{["一","二","三","四","五","六","七","八","九","十"][pidx]}',
        'description': f'408计算机学科专业基础综合模拟卷{pidx+1}',
        'duration': 120,
        'startTime': '2026-01-01 00:00:00', 'endTime': '2099-12-31 23:59:59',
        'questions': [{'questionId': str(q['id']), 'score': 5} for q in qs]
    }
    r = requests.post(f'{BASE}/paper/create', headers=H, json=data)
    if r.json()['code'] == 200:
        # 获取刚创建的试卷id(最新)
        r2 = requests.get(f'{BASE}/paper/list', headers=H)
        papers = sorted(r2.json()['data'], key=lambda x: str(x['id']), reverse=True)
        pid = papers[0]['id']
        requests.put(f'{BASE}/paper/publish/{pid}', headers=H)
        paper_ids.append(pid)
        print(f'  [已发布] 408全真模拟卷{["一","二","三","四","五","六","七","八","九","十"][pidx]} id={pid} 20题100分')
print(f'\n共创建并发布 {len(paper_ids)} 份试卷')

# ===== 全部学生模拟答题(故意出错40%) =====
students = ['23030315','student1','student2','student3','student4','student5','student6',
            'student7','student8','student9','student10','student11']

# 构建答案映射(用本次100题)
amap = {}
for q in all_q:
    amap[str(q['id'])] = q

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

random.seed(2024)
exam_count = 0
for uname in students:
    r = requests.post(f'{BASE}/user/login', json={'username':uname,'password':'123456'})
    if r.json().get('code') != 200:
        print(f'  {uname} 登录失败,跳过')
        continue
    ST = r.json()['data']['token']
    SH = {'Content-Type':'application/json','Authorization':f'Bearer {ST}'}
    for pid in paper_ids:
        r = requests.post(f'{BASE}/exam/start', headers=SH, json={'paperId': str(pid)})
        if r.json().get('code') != 200: continue
        data = r.json()['data']
        eid = data['examRecordId']
        questions = data['questions']
        for q in questions:
            qid = str(q['id'])
            correct = amap.get(qid, {}).get('answer', '')
            if not correct: continue
            ans = gen_wrong(qid, correct) if random.random() < 0.4 else correct
            requests.post(f'{BASE}/exam/answer', headers=SH, json={
                'examRecordId': str(eid), 'questionId': qid, 'userAnswer': ans
            })
        r = requests.post(f'{BASE}/exam/submit/{eid}', headers=SH)
        if r.json().get('code') == 200:
            exam_count += 1
    print(f'  {uname}: 完成全部 {len(paper_ids)} 份试卷')

print(f'\n===== 完成 =====')
print(f'新增题目: {ok}道')
print(f'新增试卷: {len(paper_ids)}份(满分100)')
print(f'学生答题: {len(students)}人 × {len(paper_ids)}卷 = {exam_count}次')
