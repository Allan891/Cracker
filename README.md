Overview PasswordCrack
------------------------------------------

PasswordCrack reads:
1. A dictionary file containing candidate words.
2. A password file containing Unix-style password records.

It then generates password candidates in several stages and compares their against the stored hashes.

The program stops as soon as all loaded passwords have been recovered, or after all implemented candidate-generation stages have been exhausted.