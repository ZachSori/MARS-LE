incrament $t0, $t0, 512 
incrament $t1, $t1, 1
incrament $t2, $t2, 2048

incrament $t3, $t3, 1
incrament $t4, $t4, 10

Income $t0
IR $t1
Loan $t2

Loop:
if< $t5, $t4, $t3
bgtz $t5, Exit

incrament $t3, $t3, 1
j Loop
Exit:
Payoff
BalTo $t9
NL
