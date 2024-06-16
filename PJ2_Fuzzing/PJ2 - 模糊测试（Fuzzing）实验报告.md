# PJ2 - 模糊测试（Fuzzing）实验报告

> 软件质量保障与测试  2023-2024春
>

## 一、分工情况

|  姓名  |    学号     |            工作内容             | 分工占比自评 |
| :----: | :---------: | :-----------------------------: | :----------: |
| 许博雅 | 21302010066 | 实现mutator策略 |              |
|  李博  | 21302010068 | 编写新的PowerSchedule，调整mutator尝试达到更多crash |              |
| 黄秋瑞 | 21302010061 | 完成PathPowerSchedule、PathGreyBoxFuzzer |              |
| 钟思祺 | 21302010069 | 修改mutator策略，尝试达到更高的crash和covered line |              |
| 宋文彦 | 21302010062 | 完成Seed磁盘持久化，debug |              |

## 二、Mutator

1. delete_random_bytes

    ```python
    def delete_random_bytes(s: str, min_length: int = 10) -> str:
        while len(s) > min_length:
            pos = random.randint(0, len(s))
            length = random.randint(0, len(s) - pos)
            s_after_delete = s[:pos] + s[pos + length:]
            if len(s_after_delete) >= min_length:
                return s_after_delete
        return s 
    ```

    实现思路：这个 Mutator 是用于随机删除字符串中的随机长度的子串。它会随机选择一个起始位置和一个长度，然后从字符串中删除从起始位置开始到指定长度之后的子串。删除操作会循环进行，直到删除后的字符串长度不小于指定的最小长度。这样可以确保变异后的字符串长度不会太短。

2. change_case

    ```python
    def change_case(s: str) -> str:
        N = random.choice([1, 2, 4])
        pos = random.randint(0, len(s) - N)
        for i in range(N):
            char = s[pos + i]
            if char.isalpha(): 
                s = s[:pos + i] + char.swapcase() + s[pos + i + 1:]
        return s
    ```

    实现思路：这个 Mutator 是用于随机选取字符串中的N个字节，然后将该位置的字母字符转换为大写或小写。首先随机选择一个位置，然后在这个位置上执行大小写转换操作，如果这个位置的字符是字母字符，则执行转换，否则不进行操作。这样可以保留非字母字符的不变性，只对字母字符进行大小写转换。

3. mutate_document_structure

    ```python
    def mutate_document_structure(s: str) -> str:
        tags = re.findall(r'<[^>]+>', s)
        
        if not tags:
            return s
        
        # 随机重排标签
        if random.choice([True, False]):
            random.shuffle(tags)
            s = ''.join(tags)
        
        # 随机嵌套和复制标签
        if random.choice([True, False]):
            selected_tag = random.choice(tags)
            times = random.randint(1, 3)  # 复制1到3次
            insertion_point = random.randint(0, len(s))
            s = s[:insertion_point] + (selected_tag * times) + s[insertion_point:]
        
        # 完全删除主要结构
        if random.choice([True, False]):
            for tag in ['<html>', '<head>', '<body>']:
                s = s.replace(tag, '')  # 删除开标签
                s = s.replace(tag.replace('<', '</'), '')  # 删除闭标签
    
        # 随机插入无效标签和属性
        if random.choice([True, False]):
            fake_tags = ['<fake>', '<nonsense nonsense="true">']
            insertion_point = random.randint(0, len(s))
            s = s[:insertion_point] + random.choice(fake_tags) + s[insertion_point:]
            
        return s
    ```

    实现思路：通过随机重排、嵌套、复制、删除和插入无效HTML标签，对输入的HTML文档结构进行变异。具体步骤包括提取所有标签，随机重排标签顺序，随机复制并插入标签，删除主要结构标签（如`<html>`、`<head>`、`<body>`），以及插入无效标签。目的是生成一个结构变异的HTML文档。


## 三、Schedule

1. CoveragePowerSchedule

   ```python
   class CoveragePowerSchedule(Schedule):
   
       def assign_energy(self, population: List[Seed]) -> None:
        for seed in population:
            novelty_score = self.novelty_scores.get(get_path_id(seed.load_coverage()), 0) 
            if novelty_score == 0:
                freq = self.path_frequency[get_path_id(seed.load_coverage())]
                novelty_score = 1 / freq
            seed.energy = novelty_score
   
   ```

   实现思路：为覆盖到新路径的种子分配更高的能量。
   在Fuzzer中获取每次新覆盖的路径，并将其出现次数记录在schedule的novelty_scores中：该新路径首次出现则赋值为1，多次出现则值++。分配种子能量时使用novelty_score代替path_frequency，对于新路径分配更高的能量，非新路径则使用频率的倒数，从而鼓励在新覆盖到的路径上进行探索。
   

## 四、新增功能实现介绍

1. Seeds 持久化

   主要思路：将所有 input 都以文件形式保存在`./seeds` 文件夹下。对所有 PASS 的 input，在 `Seed` 对象中记录该 `seed` 的存储路径（内存中仍然维护 `seed` 和 `energy` 的映射，`seed` 对象本身体量减小，减少内存负担）。在需要使用时（`scheduler` 选择好 `seed` 后）读取对应文件中的数据，对 `seed.data` 和 `seed.coverage` 分别提供获取接口。

   代码修改：在 `Seed.py` 中新增了 `seed.load_data()`、`seed.load_coverage()` 和 `seed.store()` 方法，用于读取和存储 `seed` 对应的数据、覆盖路径。新增了 `save_seed()` 用于保存所有inputs。

   `Seed` 类中的保存函数：

   ```python
   class Seed: 
       def save(self, data: str, coverage: Set[Location], directory: str = './seeds') -> None:
           '''
           @params: data: data of this seed
           @params: coverage: coverage of this seed
           @params: directory: directory of seeds storage
   
           @desc: Save seed data & coverage to disk. 
           '''
           dump_object(self.path, {
               'data': data,
               'coverage': coverage
           })
           logger.info(f"Seed saved to {self.path}")
   
       def load_data(self) -> str:
           '''
           @desc: Load seed data from disk. 
           '''
           if not os.path.exists(self.path):
               logger.warning("Seed path is not set. Nothing to load.")
               raise(FileNotFoundError(f"Seed file not found: {self.path}"))
               return None
           seed = load_object(self.path)
           data = seed['data']
           # coverage = seed['coverage']
           logger.info(f"Seed data loaded from {self.path}")
           return data
   
       def load_coverage(self) -> str:
           '''
           @desc: Load seed coverage from disk. 
           '''
           if not os.path.exists(self.path):
               logger.warning("Seed path is not set. Nothing to load.")
               raise(FileNotFoundError(f"Seed file not found: {self.path}"))
               return None
           seed = load_object(self.path)
           # data = seed['data']
           coverage = seed['coverage']
           logger.info(f"Seed coverage loaded from {self.path}")
           return coverage
   ```

   保存 FAIL input 的函数：

   ```python
   def save_seed(data: str, coverage: str, path: str=None, seed_dir: str='./seeds'):
       pass
       save_path = os.path.join(seed_dir, path)
       dump_object(save_path, {
           'data': data,
           'coverage': coverage
       })
       logger.info(f"Seed saved to {save_path}")
   ```

   

## 五、其他功能

