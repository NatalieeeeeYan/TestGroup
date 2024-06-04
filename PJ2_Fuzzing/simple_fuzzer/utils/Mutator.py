import math
import random
import struct
from typing import Any

# xby 注解：看起来seed里都是ascii的字符，所以bit操作需要先进行转换，而一个字符对应一个byte。
# 127：DEL。但不知道为什么这里保留了这个范围。

def insert_random_character(s: str) -> str:
    """
    向 s 中下标为 pos 的位置插入一个随机 byte
    pos 为随机生成，范围为 [0, len(s)]
    插入的 byte 为随机生成，范围为 [32, 127]
    """
    pos = random.randint(0, len(s))
    insert_byte = random.randint(32, 127)
    s = s[:pos] + struct.pack('B', insert_byte).decode() + s[pos:]
    return s


def flip_random_bits(s: str) -> str:
    """
    基于 AFL 变异算法策略中的 bitflip 与 random havoc 实现相邻 N 位翻转（N = 1, 2, 4），其中 N 为随机生成
    从 s 中随机挑选一个 bit，将其与其后面 N - 1 位翻转（翻转即 0 -> 1; 1 -> 0）
    注意：不要越界
    """
    binary_string = ""
    for char in s:
        # 将字符转换为ASCII码，然后将ASCII码转换为二进制字符串
        binary_char = bin(ord(char))[2:].zfill(8)  
        binary_string += binary_char
    N = random.choice([1, 2, 4])
    if len(binary_string) < N:
        pos = 0 
    else:
        pos = random.randint(0, len(binary_string) - N)
    flip_bits = binary_string[pos:pos + N]
    flip_bits = ''.join(['1' if bit == '0' else '0' for bit in flip_bits])
    binary_string = binary_string[:pos] + flip_bits + binary_string[pos + N:]
    s = ''
    for i in range(0, len(binary_string), 8):
        # 检查是否越界
        b = int(binary_string[i:i + 8], 2)
        if b < 32:
            b = 32
        elif b > 127:
            b = 127
        s += chr(b)
    return s


def arithmetic_random_bytes(s: str) -> str:
    """
    基于 AFL 变异算法策略中的 arithmetic inc/dec 与 random havoc 实现相邻 N 字节随机增减（N = 1, 2, 4），其中 N 为随机生成
    字节随机增减：
        1. 取其中一个 byte，将其转换为数字 num1；
        2. 将 num1 加上一个 [-35, 35] 的随机数，得到 num2；
        3. 用 num2 所表示的 byte 替换该 byte
    从 s 中随机挑选一个 byte，将其与其后面 N - 1 个 bytes 进行字节随机增减
    注意：不要越界；如果出现单个字节在添加随机数之后，可以通过取模操作使该字节落在 [32, 127] 之间  # 修改了范围
    """
    if len(s) == 0:
        return s  # 空字符串时返回原字符串
    N = random.choice([1, 2, 4])
    if len(s) < N:
        pos = 0
        N = 1
    else:
        pos = random.randint(0, len(s) - N)
    for i in range(N):
        num1 = ord(s[pos + i])
        num2 = num1 + random.randint(-35, 35)
        if num2 < 32:
            num2 = 32
        elif num2 > 127:
            num2 = 127
        s = s[:pos + i] + struct.pack('B', num2).decode() + s[pos + i + 1:]
    return s


def interesting_random_bytes(s: str) -> str:
    """
    基于 AFL 变异算法策略中的 interesting values 与 random havoc 实现相邻 N 字节随机替换为 interesting_value（N = 1, 2, 4），其中 N 为随机生成
    interesting_value 替换：
        1. 构建分别针对于 1, 2, 4 bytes 的 interesting_value 数组；
        2. 随机挑选 s 中相邻连续的 1, 2, 4 bytes，将其替换为相应 interesting_value 数组中的随机元素；
    注意：不要越界
    """
    interesting_values = [
        [127],
        [36],
        [32],
        [127, 127], 
        [32, 32], 
        [36, 36],
        [127, 127, 127, 127],
        [32, 32, 32, 32],
        [36, 36, 36, 36]
    ]
    pick = random.choice(interesting_values)
    N = len(pick)
    if len(s) < N:
        pos = 0
    else:
        pos = random.randint(0, len(s) - N)
    s = s[:pos] + ''.join([struct.pack('B', byte).decode() for byte in pick]) + s[pos + N:]
    return s


def havoc_random_insert(s: str):
    """
    基于 AFL 变异算法策略中的 random havoc 实现随机插入
    随机选取一个位置，插入一段的内容，其中 75% 的概率是插入原文中的任意一段随机长度的内容，25% 的概率是插入一段随机长度的 bytes
    """
    length = random.randint(0, len(s))  # 随机长度
    pos = random.randint(0, len(s))  # 随机位置
    probablity = random.random()
    if probablity < 0.75:
        start = random.randint(0, len(s) - length)
        insert = s[start:start + length]
    else:
        insert = ''.join([struct.pack('B', random.randint(32, 127)).decode() for _ in range(length)])
    s = s[:pos] + insert + s[pos:]
    return s


def havoc_random_replace(s: str):
    """
    基于 AFL 变异算法策略中的 random havoc 实现随机替换
    随机选取一个位置，替换随后一段随机长度的内容，其中 75% 的概率是替换为原文中的任意一段随机长度的内容，25% 的概率是替换为一段随机长度的 bytes
    """
    pos = random.randint(0, len(s))
    length = random.randint(0, len(s) - pos)
    probablity = random.random()
    if probablity < 0.75:
        start = random.randint(0, len(s) - length)
        replace = s[start:start + length]
    else:
        replace = ''.join([struct.pack('B', random.randint(32, 127)).decode() for _ in range(length)])
    s = s[:pos] + replace + s[pos + length:]
    return s


################################################## 以下为添加的mutator ##########################################################

def delete_random_bytes(s: str, min_length: int = 10) -> str:
    """
    基于 AFL 变异算法策略中的 delete byte 实现随机删除
    随机选取一个位置，删除随后一段随机长度的内容，但要保证删除后字符串长度不小于 min_length
    """
    if len(s) <= min_length:
        return s 
    
    while len(s) > min_length:
        pos = random.randint(0, len(s))
        length = random.randint(0, len(s) - pos)
        s_after_delete = s[:pos] + s[pos + length:]
        if len(s_after_delete) >= min_length:
            return s_after_delete
    return s 


def change_case(s: str) -> str:
    """
    随机选取 N 字节（N = 1, 2, 4），将该位置的字符转换为大写或小写
    """
    if len(s) == 0:
        return s  # 空字符串时返回原字符串
    N = random.choice([1, 2, 4])
    if len(s) < N:
        pos = 0
        N = random.randint(1, len(s))
    else:
        pos = random.randint(0, len(s) - N)
    for i in range(N):
        char = s[pos + i]
        if char.isalpha():  # 检查字符是否是字母字符
            s = s[:pos + i] + char.swapcase() + s[pos + i + 1:]
    return s


class Mutator:

    def __init__(self) -> None:
        """Constructor"""
        self.mutators = [
            insert_random_character,
            flip_random_bits,
            arithmetic_random_bytes,
            interesting_random_bytes,
            havoc_random_insert,
            havoc_random_replace,
            delete_random_bytes,
            change_case
        ]

    def mutate(self, inp: Any) -> Any:
        mutator = random.choice(self.mutators)
        return mutator(inp)
