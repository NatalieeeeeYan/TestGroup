import math
import random
import struct
from typing import Any
import re
# xby 注解：看起来seed里都是ascii的字符，所以bit操作需要先进行转换，而一个字符对应一个byte。
# 127：DEL。但不知道为什么这里保留了这个范围。

encode = ''

def insert_random_character(s: str) -> str:
    """
    向 s 中下标为 pos 的位置插入一个随机 byte
    pos 为随机生成，范围为 [0, len(s)]
    插入的 byte 为随机生成，范围为 [32, 127]
    """
    pos = random.randint(0, len(s))
    random_character = chr(random.randrange(32, 127))
    return s[:pos] + random_character + s[pos:]


def flip_random_bits(s: str) -> str:
    """
    基于 AFL 变异算法策略中的 bitflip 与 random havoc 实现相邻 N 位翻转（N = 1, 2, 4），其中 N 为随机生成
    从 s 中随机挑选一个 bit，将其与其后面 N - 1 位翻转（翻转即 0 -> 1; 1 -> 0）
    注意：不要越界
    """
    if not s:
        return insert_random_character(s)
    
    binary_string = ""
    for char in s:
        # 将字符转换为ASCII码，然后将ASCII码转换为二进制字符串
        binary_char = bin(ord(char))[2:].zfill(8)  
        binary_string += binary_char
    N = random.choice([1, 2, 4])
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
    注意：不要越界；如果出现单个字节在添加随机数之后，可以通过取模操作使该字节落在 [0, 255] 之间
    """
    if not s:
        return insert_random_character(s)

    possible_lengths = [n for n in [1, 2, 4] if n <= len(s)]
    N= random.choice(possible_lengths)
    pos = random.randint(0, len(s) - N)
    for i in range(N):
        num1 = ord(s[pos + i])
        num2 = (num1 + random.choice([-1, 1]) * random.randint(1, 35) + 256) % 256
        s = s[:pos + i] + chr(num2) + s[pos + i + 1:]
    return s


def interesting_random_bytes(s: str) -> str:
    """
    基于 AFL 变异算法策略中的 interesting values 与 random havoc 实现相邻 N 字节随机替换为 interesting_value（N = 1, 2, 4），其中 N 为随机生成
    interesting_value 替换：
        1. 构建分别针对于 1, 2, 4 bytes 的 interesting_value 数组；
        2. 随机挑选 s 中相邻连续的 1, 2, 4 bytes，将其替换为相应 interesting_value 数组中的随机元素；
    注意：不要越界
    """
    if not s:
        return insert_random_character(s)

    interesting_values_8bit = [-128, -1, 0 , 1,  16, 60, 62, 32, 64, 100, 127] # 针对html 增加了一些值
    interesting_values_16bit = [-32768, -129, 128, 255, 256, 512, 1000, 1024, 4096, 32767] + interesting_values_8bit
    interesting_values_32bit = [-2147483648, -100663046, -32769, 32768, 65535, 65536, 100663045, 2147483647] + interesting_values_16bit

    possible_lengths = [n for n in [1, 2, 4] if n <= len(s)]
    N= random.choice(possible_lengths)
    
    pos = random.randint(0, len(s) - N)
    if N == 1:
        value = random.choice(interesting_values_8bit)
        byte_data = struct.pack('b', value)
        s = s[:pos] + byte_data.decode(errors='ignore') + s[pos + 1:]
    elif N == 2:
        value = random.choice(interesting_values_16bit)
        byte_data = struct.pack('>h', value)
        s = s[:pos] + byte_data.decode(errors='ignore') + s[pos + 2:]
    elif N == 4:
        value = random.choice(interesting_values_32bit)
        byte_data = struct.pack('>i', value)
        s = s[:pos] + byte_data.decode(errors='ignore') + s[pos + 4:]

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
    N = random.choice([1, 2, 4])
    if len(s) - N > 0:
        pos = random.randint(0, len(s) - N)
    else:
        pos = 0
    index = min(N, len(s))
    for i in range(index):
        # print(pos+i, len(s))
        char = s[pos + i]
        if char.isalpha():  # 检查字符是否是字母字符
            s = s[:pos + i] + char.swapcase() + s[pos + i + 1:]
    return s

################################################## 以下为html mutator ##########################################################

def insert_random_html_tag(s: str) -> str:
    html_tags = ["<div>", "\x00"]
    pos = random.randint(0, len(s))
    tag = random.choice(html_tags)
    return s[:pos] + tag + s[pos:]

def delete_random_html_tag(s: str) -> str:
    html_tags = ["<div>", "</div>", "<span>", "</span>", "<p>", "</p>", "<a>", "</a>", "<script>", "</script>", "<style>", "</style>"]
    for tag in html_tags:
        start = s.find(tag)
        if start != -1:
            return s[:start] + s[start + len(tag):]
    return s

def replace_random_html_tag(s: str) -> str:
    html_tags = ["<div>", "</div>", "<span>", "</span>", "<p>", "</p>", "<a>", "</a>", "<script>", "</script>", "<style>", "</style>"]
    for existing_tag in html_tags:
        if existing_tag in s:
            new_tag = random.choice(html_tags)
            return s.replace(existing_tag, new_tag, 1)
    return s

def insert_random_html_attribute(s: str) -> str:
    html_attributes = ["id", "class", "href", "style", "src"]
    pos = random.randint(0, len(s))
    attribute = random.choice(html_attributes)
    value = ''.join(random.choices('abcdefghijklmnopqrstuvwxyz', k=5))
    return s[:pos] + f' {attribute}="{value}" ' + s[pos:]

def insert_random_html_entity(s: str) -> str:
    html_entities = ["&amp;", "&lt;", "&gt;", "&quot;", "&apos;"]
    pos = random.randint(0, len(s))
    entity = random.choice(html_entities)
    return s[:pos] + entity + s[pos:]

def insert_random_javascript(s: str) -> str:
    js_code = "<script>console.log('test');</script>"
    pos = random.randint(0, len(s))
    return s[:pos] + js_code + s[pos:]

def change_html_structure(s: str) -> str:
    pos1, pos2 = sorted(random.sample(range(len(s)), 2))
    return s[:pos1] + s[pos2:] + s[pos1:pos2]

def radical_mutate_document_structure(s: str) -> str:
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

def addLbr(s:str) -> str:
    if isinstance(s, bytes):
        # 使用 chardet 检测字节对象的编码
        detected_encoding = chardet.detect(s)['encoding']
        print("addlb",detected_encoding)
        if detected_encoding == 'gbk':
            try:
                s = s.decode(detected_encoding)
                s = s.encode('GB2312')
            except (UnicodeDecodeError, TypeError) as e:
                raise ValueError(f"Failed to decode bytes: {e}")
    result = []
    for char in s:
        result.append(char)
        if char == '!':
            result.append('[')
    return ''.join(result)
    
import chardet
def change_encode(s: str):
    # 如果输入对象是字节对象（bytes），则先尝试解码为字符串
    if isinstance(s, bytes):
        # 使用 chardet 检测字节对象的编码
        detected_encoding = chardet.detect(s)['encoding']
        print(detected_encoding)
        encode = detected_encoding
        try:
            s = s.decode(detected_encoding)
        except (UnicodeDecodeError, TypeError) as e:
            raise ValueError(f"Failed to decode bytes: {e}")
    return s.encode('gbk')

def my_insert(s: str) -> str:
    pos = random.randint(0, len(s))
    chars = ["<", ">", "<!"]
    random_character = random.choice(chars)
    return s[:pos] + random_character + s[pos:]

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
            change_case,
            # radical_mutate_document_structure,
            # insert_random_html_tag,
            # delete_random_html_tag,
            # replace_random_html_tag,
            # insert_random_html_attribute,
            # insert_random_html_entity,
            # insert_random_javascript,
            # change_html_structure,
            # addLbr,
            # change_encode,
            my_insert,
        ]

    def mutate(self, inp: Any) -> Any:
        mutator = random.choice(self.mutators)
        # for _ in range(random.randint(1, 3)):  # 每次调用 1 到 3 个变异操作
        #     mutator = random.choice(self.mutators)
        #     inp = mutator(inp)
        return mutator(inp)
