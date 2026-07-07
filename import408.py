import requests, json, sys

BASE = 'http://localhost:8080'

# login as teacher1
r = requests.post(f'{BASE}/user/login', json={'username':'teacher1','password':'123456'})
TOKEN = r.json()['data']['token']
H = {'Content-Type':'application/json', 'Authorization': f'Bearer {TOKEN}'}

questions = [
    # ===== 数据结构 8 =====
    {'type':'single_choice','category':'数据结构','content':'对n个关键字进行快速排序，平均时间复杂度是？','optionA':'O(n)','optionB':'O(nlogn)','optionC':'O(n^2)','optionD':'O(logn)','answer':'B','difficulty':'easy','score':2},
    {'type':'single_choice','category':'数据结构','content':'在单链表中删除某个已知节点，时间复杂度是？','optionA':'O(1)','optionB':'O(n)','optionC':'O(nlogn)','optionD':'O(n^2)','answer':'B','difficulty':'easy','score':2},
    {'type':'single_choice','category':'数据结构','content':'以下哪种树不是二叉搜索树？','optionA':'AVL树','optionB':'红黑树','optionC':'B+树','optionD':'哈夫曼树','answer':'D','difficulty':'medium','score':2},
    {'type':'single_choice','category':'数据结构','content':'有向图中所有顶点的入度之和等于？','optionA':'顶点数','optionB':'边数的2倍','optionC':'边数','optionD':'0','answer':'C','difficulty':'medium','score':2},
    {'type':'multi_choice','category':'数据结构','content':'以下哪些是图的存储方式？','optionA':'邻接矩阵','optionB':'邻接表','optionC':'十字链表','optionD':'邻接多重表','answer':'A,B,C,D','difficulty':'medium','score':2},
    {'type':'true_false','category':'数据结构','content':'二叉搜索树的中序遍历序列一定有序。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'A','difficulty':'easy','score':2},
    {'type':'true_false','category':'数据结构','content':'哈希表平均查找长度与冲突处理方法无关。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'B','difficulty':'medium','score':2},
    {'type':'fill_blank','category':'数据结构','content':'n个节点的完全二叉树中，叶子节点最多为______个。','optionA':'','optionB':'','optionC':'','optionD':'','answer':'n/2','difficulty':'hard','score':2},

    # ===== 计算机组成原理 8 =====
    {'type':'single_choice','category':'计算机组成原理','content':'冯诺依曼计算机中指令和数据存于同一存储器，称为？','optionA':'哈佛结构','optionB':'普林斯顿结构','optionC':'RISC结构','optionD':'CISC结构','answer':'B','difficulty':'easy','score':2},
    {'type':'single_choice','category':'计算机组成原理','content':'CPU响应中断的时间是？','optionA':'一条指令执行结束','optionB':'一个总线周期结束','optionC':'一个时钟周期结束','optionD':'一个机器周期结束','answer':'A','difficulty':'medium','score':2},
    {'type':'single_choice','category':'计算机组成原理','content':'微程序控制器中微指令存储在？','optionA':'主存','optionB':'Cache','optionC':'控制存储器','optionD':'通用寄存器','answer':'C','difficulty':'medium','score':2},
    {'type':'multi_choice','category':'计算机组成原理','content':'以下哪些属于CISC架构特点？','optionA':'指令长度可变','optionB':'指令数量多','optionC':'寻址方式多样','optionD':'指令长度固定','answer':'A,B,C','difficulty':'medium','score':2},
    {'type':'multi_choice','category':'计算机组成原理','content':'以下哪些是常见寻址方式？','optionA':'立即寻址','optionB':'直接寻址','optionC':'间接寻址','optionD':'变址寻址','answer':'A,B,C,D','difficulty':'easy','score':2},
    {'type':'true_false','category':'计算机组成原理','content':'流水线技术可提高单条指令执行速度。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'B','difficulty':'medium','score':2},
    {'type':'fill_blank','category':'计算机组成原理','content':'指令周期包括取指周期、______和执行周期。','optionA':'','optionB':'','optionC':'','optionD':'','answer':'间址周期','difficulty':'hard','score':2},
    {'type':'fill_blank','category':'计算机组成原理','content':'总线仲裁分为集中式仲裁和______仲裁。','optionA':'','optionB':'','optionC':'','optionD':'','answer':'分布式','difficulty':'medium','score':2},

    # ===== 操作系统 7 =====
    {'type':'single_choice','category':'操作系统','content':'以下哪种不是进程间通信方式？','optionA':'管道','optionB':'消息队列','optionC':'共享内存','optionD':'轮询','answer':'D','difficulty':'easy','score':2},
    {'type':'single_choice','category':'操作系统','content':'段页式存储管理中访问一个数据需访问内存几次？','optionA':'1次','optionB':'2次','optionC':'3次','optionD':'4次','answer':'C','difficulty':'medium','score':2},
    {'type':'single_choice','category':'操作系统','content':'哪种磁盘调度算法可保证最短寻道时间？','optionA':'FCFS','optionB':'SSTF','optionC':'SCAN','optionD':'C-SCAN','answer':'B','difficulty':'medium','score':2},
    {'type':'multi_choice','category':'操作系统','content':'以下哪些是进程调度算法？','optionA':'先来先服务','optionB':'短作业优先','optionC':'高响应比优先','optionD':'时间片轮转','answer':'A,B,C,D','difficulty':'easy','score':2},
    {'type':'true_false','category':'操作系统','content':'SPOOLing技术可将独占设备改造为共享设备。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'A','difficulty':'medium','score':2},
    {'type':'true_false','category':'操作系统','content':'系统处于安全状态就一定不会发生死锁。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'A','difficulty':'medium','score':2},
    {'type':'fill_blank','category':'操作系统','content':'分页系统中逻辑地址分为页号和______。','optionA':'','optionB':'','optionC':'','optionD':'','answer':'页内偏移','difficulty':'easy','score':2},

    # ===== 计算机网络 7 =====
    {'type':'single_choice','category':'计算机网络','content':'以下哪种设备工作在网络层？','optionA':'集线器','optionB':'交换机','optionC':'路由器','optionD':'中继器','answer':'C','difficulty':'easy','score':2},
    {'type':'single_choice','category':'计算机网络','content':'ARP协议的作用是？','optionA':'IP转MAC','optionB':'域名转IP','optionC':'MAC转IP','optionD':'端口转IP','answer':'A','difficulty':'easy','score':2},
    {'type':'single_choice','category':'计算机网络','content':'TCP建立连接需要几次握手？','optionA':'1次','optionB':'2次','optionC':'3次','optionD':'4次','answer':'C','difficulty':'easy','score':2},
    {'type':'multi_choice','category':'计算机网络','content':'以下哪些是网络层协议？','optionA':'IP','optionB':'ICMP','optionC':'TCP','optionD':'ARP','answer':'A,B,D','difficulty':'medium','score':2},
    {'type':'true_false','category':'计算机网络','content':'UDP提供可靠的数据传输服务。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'B','difficulty':'easy','score':2},
    {'type':'true_false','category':'计算机网络','content':'DNS使用TCP端口53进行域名解析。','optionA':'正确','optionB':'错误','optionC':'','optionD':'','answer':'B','difficulty':'medium','score':2},
    {'type':'fill_blank','category':'计算机网络','content':'IPv6地址长度为______位。','optionA':'','optionB':'','optionC':'','optionD':'','answer':'128','difficulty':'easy','score':2},
]

ok = 0
for i, q in enumerate(questions):
    r = requests.post(f'{BASE}/question/add', headers=H, json=q)
    if r.json()['code'] == 200: ok += 1
    print(f'  [{i+1:02d}/30] {"OK" if r.json()["code"]==200 else "FAIL"} {q["category"]} {q["type"]}')
print(f'\nImported: {ok}/30')
