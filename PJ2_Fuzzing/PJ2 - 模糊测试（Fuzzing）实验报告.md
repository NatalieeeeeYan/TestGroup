# PJ2 - 模糊测试（Fuzzing）实验报告

软件质量保障与测试  2023-2024春

## 一、分工情况

| 姓名 | 学号 | 工作内容 | 分工占比自评 |
| :--: | :--: | :------: | :----------: |
|      |      |          |              |
|      |      |          |              |
|      |      |          |              |
|      |      |          |              |
|      |      |          |              |
|      |      |          |              |

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
