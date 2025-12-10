incrament $t0, $t0, 1
incrament $t1, $t1, 10
Income $t0

Avg $t2, $t1, $t0
Loop:
if< $t3, $t1, $t2
gtgz $t3 After
comb $t2, $t2, $t0
goto Loop

After:

incrament $t4, $t1, 50
dif $t4, $t4, $t2
incrament $t5, $t5, 2
product $t2, $t2, $t5
quot $t4, $t4, $t2
dif $t4, $t4, $t0
exor $t9, $t4, $t0
gtgz $t9, end
Status
eor $t8, $t4, $t0
end:
NL


