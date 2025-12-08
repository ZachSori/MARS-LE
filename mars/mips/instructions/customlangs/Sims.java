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
                new BasicInstruction("incrament $t0,$t1,12",
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
                     // Print "hello" to the MARS I/O emulator (Messages pane or stdout)
                     mars.util.SystemIO.printString("hello\n");
                     
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
                  }
               }));


    }
}