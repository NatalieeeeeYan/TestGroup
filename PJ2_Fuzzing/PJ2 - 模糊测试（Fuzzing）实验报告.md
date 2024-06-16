# PJ2 - 模糊测试（Fuzzing）实验报告

> 软件质量保障与测试  2023-2024春
>

## 一、分工情况

|  姓名  |    学号     |            工作内容             | 分工占比自评 |
| :----: | :---------: | :-----------------------------: | :----------: |
| 许博雅 |             |                                 |              |
|  李博  |             |                                 |              |
| 黄秋瑞 |             |                                 |              |
| 钟思祺 |             |                                 |              |
| 宋文彦 | 21302010062 | 完成Seed磁盘持久化，全流程debug |              |

##### 内容：

1. 完善 6 个基本 mutators：【xby】
   - insert_random_character
   - flip_random_bits
   - arithmetic_random_bytes
   - interesting_random_bytes
   - havoc_random_insert
   - havoc_random_replace
   - 编写 1 个附加 mutator
2. 完善原有 PathPowerSchedule【hqr & lb】
   - 编写 1 个新的 PowerSchedule
3. 完善 PathGreyBoxFuzzer【hqr & lb】
4. 调整代码 完成测试【zsq & swy】

ddl：1-3 6/1之前，4 6/16之前



## 二、Mutator

`在本章中，阐述你新增的 Mutator 的代码以及实现思路，以下为示例`

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


## 三、Schedule

`在本章中，你需要阐述你新增编写的 Schedule 的实现思路,以下为示例`

1. LevelPowerSchedule

   ```python
   class LevelPowerSchedule(Schedule):
   
       def assign_energy(self, population: Sequence[Seed]) -> None:
           ...
   
   ```

   实现思路：基于种子变异的层级...

## 四、新增功能实现介绍

`介绍你们在实现将 input 动态存储本地的过程中的设计思路以及实现效果`

## 五、其他功能

### 4.1 Seeds 持久化

主要思路：将所有 input 都以文件形式保存在`./seeds` 文件夹下。对所有 PASS 的 input，在 `Seed` 对象中记录该 `seed` 的存储路径（内存中仍然维护 `seed` 和 `energy` 的映射，`seed` 对象本身体量减小，减少内存负担）。在需要使用时（`scheduler` 选择好 `seed` 后）读取对应文件中的数据，对 `seed.data` 和 `seed.coverage` 分别提供获取接口。

代码修改：在 `Seed.py` 中新增了 `seed.load_data()`、`seed.load_coverage()` 和 `seed.store()` 方法，用于读取和存储 `seed` 对应的数据、覆盖路径。新增了 `save_seed()` 用于保存所有inputs。
