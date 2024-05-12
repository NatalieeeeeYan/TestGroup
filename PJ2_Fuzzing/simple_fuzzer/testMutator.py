from utils.Mutator import *

s = "hello, world!"
print("Original string:", s)
print("Insert random character:", insert_random_character(s))
print("Flip random bits:", flip_random_bits(s))
print("Arithmetic random bytes:", arithmetic_random_bytes(s))
print("Interesting random bytes:", interesting_random_bytes(s))
print("Random insert:", havoc_random_insert(s))
print("Random replace:", havoc_random_replace(s))
print("Delete random bytes:", delete_random_bytes(s))
print("Change case:", change_case(s))

