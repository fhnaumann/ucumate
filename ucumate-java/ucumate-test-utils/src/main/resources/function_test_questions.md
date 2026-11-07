# Questions

### Unit 10+3/ul was expected to be invalid, but was valid (1-108)

In ucum-java any integer unit with an exponent is invalid (i.e. 5*3).
In ucum-lhc only integer units with one place (0-9) may be raised to an exponent.

### 3-126

V --- canonical ---> g.m/s2.m/C (correct)
A --- canonical ---> C/s (correct)
Ohm --- canonical ---> g.m/s2.m/C/C/s (incorrect) update, now its: ((((g.m/s2).m)/C)/(C/s)) and it works
I think the "C/s" from the "A" should be in brackets, dont know why it isnt. Update: It now works.
But its still wrong for S.
                                      | why is it s-1 instead of s-2???
S -- canonical ---> (((((((g-1.((m-1/s-1)))).m-1))/C-1))/((C-1/s-1))) (incorrect)
Maybe something wrong with the Math.abs() part in the map? Or somewhere else during canonicalization?