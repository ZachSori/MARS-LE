    package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.mips.instructions.syscalls.*;
    import mars.*;
    import mars.util.*;
    import java.util.*;
    import java.io.*;
    import mars.mips.instructions.*;
    import java.util.Random;
    


public class Sims extends CustomAssembly{
    public static int balance, debt, income, ir, lir = 0;
    public static int year = 0;

    public void reset(){
        balance = 0;
        debt = 0;
        income = 0;
        ir = 0;
        lir = 0;
        year = 0;
    }
    public void pass(int lineNum){
        if(lineNum==1){
            reset();
        }
        year++;
        balance += income;
        if(lir>0){
            debt += (debt * lir) / 100;
        }
        if(ir>0){
            balance += (balance * ir) / 100;
        }
    }
    public String getStats(){
        return "Year: " + year + "\nBalance: " + balance + "\nDebt: " + debt + "\nIncome: " + income + "\nInterest Rate: " + ir + "%\nLoan Interest Rate: " + lir + "%";
    }
    @Override
    public String getName(){
        return "Sims Assembly";
    }

    @Override
    public String getDescription(){
        return "Just to make sure we've got this all straight";
    }
    
    @Override
    protected void populate(){

        // put (same as addi)
      instructionList.add(
                new BasicInstruction("incrament $t0,$t1,-65535",
            	 "Assign value to register: set $t0 to ($t1 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT,
                "111111 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int placeholder = RegisterFile.getValue(operands[1]);
                     int value = operands[2] << 16 >> 16;
                     int result = placeholder + value;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((placeholder >= 0 && value >= 0 && result < 0)
                        || (placeholder < 0 && value < 0 && result >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], result);
                     int line = statement.getSourceLine();
                     pass(line);

                     
                  }
               }));
            instructionList.add(
                new BasicInstruction("comb $t1,$t2,$t3",
            	 "Addition with overflow : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = RegisterFile.getValue(operands[2]);
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                     int line = statement.getSourceLine();
                     pass(line);
                  }
               }));

               instructionList.add(
                new BasicInstruction("dif $t1,$t2,$t3",
            	 "Subtraction with overflow : set $t1 to ($t2 minus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int sub1 = RegisterFile.getValue(operands[1]);
                     int sub2 = RegisterFile.getValue(operands[2]);
                     int dif = sub1 - sub2;
                    if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                        || (sub1 < 0 && sub2 >= 0 && dif >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], dif);
                     int line = statement.getSourceLine();
                     pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("product $t1,$t2,$t3",
            	 "Multiplication without overflow  : Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3 (use mfhi to access HI, mflo to access LO)",
                BasicInstructionFormat.R_FORMAT,
                "011100 sssss ttttt fffff 00000 000010",
                new SimulationCode()
                {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     long product = (long) RegisterFile.getValue(operands[1])
                        * (long) RegisterFile.getValue(operands[2]);
                     RegisterFile.updateRegister(operands[0],
                        (int) ((product << 32) >> 32));
                  // Register 33 is HIGH and 34 is LOW.  Not required by MIPS; SPIM does it.
                     RegisterFile.updateRegister(33, (int) (product >> 32));
                     RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                     int line = statement.getSourceLine();
                     pass(line);
                  }
               }));
               
               instructionList.add(
                new BasicInstruction("quot $t1,$t2",
            	 "Division with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss 00000 00000 011010",
                new SimulationCode()
                {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[1]) == 0)
                     {
                     // Note: no exceptions and undefined results for zero div
                     // COD3 Appendix A says "with overflow" but MIPS 32 instruction set
                     // specification says "no arithmetic exception under any circumstances".
                        return;
                     }
                  
                  // Register 33 is HIGH and 34 is LOW
                     RegisterFile.updateRegister(33,
                        RegisterFile.getValue(operands[0])
                        % RegisterFile.getValue(operands[1]));
                     RegisterFile.updateRegister(34,
                        RegisterFile.getValue(operands[0])
                        / RegisterFile.getValue(operands[1]));
                        int line = statement.getSourceLine();
                        pass(line);
                  }
                }));

                instructionList.add(
                //new BasicInstruction("sw $t1,-100($t2)",
                new BasicInstruction("Store $t1 in $t2 + 100",
                "Store word : Store contents of $t1 into effective memory word address",
            	 BasicInstructionFormat.I_FORMAT,
                "101011 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        Globals.memory.setWord(
                            RegisterFile.getValue(operands[1]) + operands[2],
                            RegisterFile.getValue(operands[0]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                        int line = statement.getSourceLine();
                        pass(line);
                  }
               }));

               instructionList.add(
                new BasicInstruction("lw $t1,-100($t2)",
            	 "Load word : Set $t1 to contents of effective memory word address",
                BasicInstructionFormat.I_FORMAT,
                "100011 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        RegisterFile.updateRegister(operands[0],
                            Globals.memory.getWord(
                            RegisterFile.getValue(operands[2]) + operands[1]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                        int line = statement.getSourceLine();
                        pass(line);
                  }
               }));

               instructionList.add(
                new BasicInstruction("or $t1,$t2,$t3",
            	 "Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100101",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        RegisterFile.getValue(operands[1])
                        | RegisterFile.getValue(operands[2]));
                        int line = statement.getSourceLine();
                        pass(line);
                  }
               }));

               instructionList.add(
                new BasicInstruction("xor $t1,$t2,$t3",
            	 "Bitwise XOR (exclusive OR) : Set $t1 to bitwise XOR of $t2 and $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100110",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        RegisterFile.getValue(operands[1])
                        ^ RegisterFile.getValue(operands[2]));
                        int line = statement.getSourceLine();
                        pass(line);
                  }
               }));

               instructionList.add(
                new BasicInstruction("if< $t1,$t2,$t3",
                "Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        (RegisterFile.getValue(operands[1])
                        < RegisterFile.getValue(operands[2]))
                                ? 1
                                : 0);
                                int line = statement.getSourceLine();
                                pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("j target", 
            	 "Jump unconditionally : Jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));
               instructionList.add(
                new BasicInstruction("bltz $t1,label",
                "Branch if less than zero : Branch to statement at label's address if $t1 is less than zero",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000001 fffff 00000 ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[0]) < 0)
                     {
                        Globals.instructionSet.processBranch(operands[1]);
                     }
                  }
               }));
               instructionList.add(
                new BasicInstruction("bgtz $t1,label",
                "Branch if greater than zero : Branch to statement at label's address if $t1 is greater than zero",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000111 fffff 00000 ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[0]) > 0)
                     {
                        Globals.instructionSet.processBranch(operands[1]);
                     }
                  }
               }));
               //-------------------------------Unique Instructions--------------------------------//
               instructionList.add(
                new BasicInstruction("Avg $t1,$t2,$t3",
            	 "Addition with overflow : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = RegisterFile.getValue(operands[2]);
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     int average = (int)(((long)add1 + (long)add2) / 2L);
                     RegisterFile.updateRegister(operands[0], average);
                     int line = statement.getSourceLine();
                     pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("Status",
                "Status Report : Print current financial status to the MARS I/O emulator (Messages pane or stdout)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    mars.util.SystemIO.printString(getStats() + "\n");
                    int line = statement.getSourceLine();
                    pass(line);

                  }
               }));
               instructionList.add(
                new BasicInstruction("Income $t0",
                "Sets Income : Set income to register value",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    income = RegisterFile.getValue(operands[0]);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("Loan $t0",
                "Sets Debt: Set debt to register value, LIR is also applied depending on debt size",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    debt += RegisterFile.getValue(operands[0]);
                    if(debt>0 && lir==0) lir = 5;
                    else if(debt==0) lir = 0;
                    else if(debt>1000) lir = 10;
                    else if(debt>5000) lir = 15;
                    else if(debt>10000) lir = 25;
                    else if(debt>50000) lir = 50;
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("IR $t0",
                "Sets Interest Rate : Set interest rate to register value",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    ir = RegisterFile.getValue(operands[0]);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("LIR $t0",
                "Sets Interest Rate : Set loan interest rate to register value",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    lir = RegisterFile.getValue(operands[0]);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("Savings $t0",
                "Increase Balance: Increase balance by register value",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    balance += RegisterFile.getValue(operands[0]);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("NL",
                "New Life: Resets the progress you have and start from 0 again",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    mars.util.SystemIO.printString("Your life has been fufilled and you have reached Agartha");
                    mars.util.SystemIO.printString("\nYou lived for " + year + " years.");
                    if(debt==0){
                        mars.util.SystemIO.printString("\nYou passed debt-free with a net worth of " + balance + ".");
                    }
                    else if(balance>=debt){
                        mars.util.SystemIO.printString("\nYou passed a wealthy person with a net worth of " + balance + " and an unpaid debt of " + debt + ".");
                    } else {
                        mars.util.SystemIO.printString("\nYou died in poverty with a balance of " + balance + " and a debt of " + debt + ".");
                    }
                    reset();
                  }
               }));
               instructionList.add(
                new BasicInstruction("Payoff",
                "New Life: Resets the progress you have and start from 0 again",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    if(balance>=debt){
                        balance -= debt;
                        debt = 0;
                        lir = 0;
                        mars.util.SystemIO.printString("You are debt-free!\nYou have a remaining balance of " + balance + ".\n");
                    } else {
                        debt -= balance;
                        mars.util.SystemIO.printString("You have paid off " + balance + " of your debt.");
                        mars.util.SystemIO.printString("\nYou still owe " + debt + ".");
                        balance = 0;
                    }
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               
               instructionList.add(
                new BasicInstruction("BalTo $t0",
                "Sets the register to a unique value (Balance, Debt, Income, IR, LIR, Year)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    RegisterFile.updateRegister(operands[0], balance);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));
               instructionList.add(
                new BasicInstruction("DebtTo $t0",
                "Sets the register to a unique value (Balance, Debt, Income, IR, LIR, Year)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int [] operands = statement.getOperands();
                    RegisterFile.updateRegister(operands[0], debt);
                    int line = statement.getSourceLine();
                    pass(line);
                  }
               }));


    }
}