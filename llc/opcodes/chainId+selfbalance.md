https://github.com/winsvega/solidity/commit/fd3ae0b24a8490ae03f8198af645a51f377cb2da

```diff
From fd3ae0b24a8490ae03f8198af645a51f377cb2da Mon Sep 17 00:00:00 2001
From: Dimitry <dimitry@ethereum.org>
Date: Mon, 2 Sep 2019 16:23:45 +0300
Subject: [PATCH] add chainid and selfbalance to lllc

---
 libevmasm/GasMeter.cpp                                  | 6 ++++++
 libevmasm/Instruction.cpp                               | 4 ++++
 libevmasm/Instruction.h                                 | 2 ++
 libevmasm/SemanticInformation.cpp                       | 3 +++
 liblangutil/EVMVersion.cpp                              | 4 ++++
 liblangutil/EVMVersion.h                                | 2 ++
 libyul/AsmAnalysis.cpp                                  | 8 ++++++++
 test/liblll/Compiler.cpp                                | 2 ++
 test/tools/yulInterpreter/EVMInstructionInterpreter.cpp | 4 ++++
 test/tools/yulInterpreter/Interpreter.h                 | 2 ++
 10 files changed, 37 insertions(+)

diff --git a/libevmasm/GasMeter.cpp b/libevmasm/GasMeter.cpp
index d98b3efa97f..5ff3a905ecf 100644
--- a/libevmasm/GasMeter.cpp
+++ b/libevmasm/GasMeter.cpp
@@ -188,6 +188,12 @@ GasMeter::GasConsumption GasMeter::estimateMax(AssemblyItem const& _item, bool _
 		case Instruction::BALANCE:
 			gas = GasCosts::balanceGas(m_evmVersion);
 			break;
+		case Instruction::CHAINID:
+			gas = runGas(Instruction::CHAINID);
+			break;
+		case Instruction::SELFBALANCE:
+			gas = runGas(Instruction::SELFBALANCE);
+			break;
 		default:
 			gas = runGas(_item.instruction());
 			break;
diff --git a/libevmasm/Instruction.cpp b/libevmasm/Instruction.cpp
index 6dd9da6b91f..cc5561dafae 100644
--- a/libevmasm/Instruction.cpp
+++ b/libevmasm/Instruction.cpp
@@ -81,6 +81,8 @@ std::map<std::string, Instruction> const dev::eth::c_instructions =
 	{ "NUMBER", Instruction::NUMBER },
 	{ "DIFFICULTY", Instruction::DIFFICULTY },
 	{ "GASLIMIT", Instruction::GASLIMIT },
+	{ "CHAINID", Instruction::CHAINID },
+	{ "SELFBALANCE", Instruction::SELFBALANCE },
 	{ "POP", Instruction::POP },
 	{ "MLOAD", Instruction::MLOAD },
 	{ "MSTORE", Instruction::MSTORE },
@@ -225,6 +227,8 @@ static std::map<Instruction, InstructionInfo> const c_instructionInfo =
 	{ Instruction::NUMBER,		{ "NUMBER",			0, 0, 1, false, Tier::Base } },
 	{ Instruction::DIFFICULTY,	{ "DIFFICULTY",		0, 0, 1, false, Tier::Base } },
 	{ Instruction::GASLIMIT,	{ "GASLIMIT",		0, 0, 1, false, Tier::Base } },
+	{ Instruction::CHAINID,		{ "CHAINID",		0, 0, 1, false, Tier::Base } },
+	{ Instruction::SELFBALANCE,	{ "SELFBALANCE",	0, 0, 1, false, Tier::Low } },
 	{ Instruction::POP,			{ "POP",			0, 1, 0, false, Tier::Base } },
 	{ Instruction::MLOAD,		{ "MLOAD",			0, 1, 1, true, Tier::VeryLow } },
 	{ Instruction::MSTORE,		{ "MSTORE",			0, 2, 0, true, Tier::VeryLow } },
diff --git a/libevmasm/Instruction.h b/libevmasm/Instruction.h
index 9a0a5d4e34f..f2f5879d983 100644
--- a/libevmasm/Instruction.h
+++ b/libevmasm/Instruction.h
@@ -90,6 +90,8 @@ enum class Instruction: uint8_t
 	NUMBER,				///< get the block's number
 	DIFFICULTY,			///< get the block's difficulty
 	GASLIMIT,			///< get the block's gas limit
+	CHAINID,			///< get the config's chainid param
+	SELFBALANCE,		///< get balance of the current account

 	POP = 0x50,			///< remove item from stack
 	MLOAD,				///< load word from memory
diff --git a/libevmasm/SemanticInformation.cpp b/libevmasm/SemanticInformation.cpp
index 6b6b4611dd2..8932d43513e 100644
--- a/libevmasm/SemanticInformation.cpp
+++ b/libevmasm/SemanticInformation.cpp
@@ -172,6 +172,7 @@ bool SemanticInformation::isDeterministic(AssemblyItem const& _item)
 	case Instruction::PC:
 	case Instruction::MSIZE: // depends on previous writes and reads, not only on content
 	case Instruction::BALANCE: // depends on previous calls
+	case Instruction::SELFBALANCE: // depends on previous calls
 	case Instruction::EXTCODESIZE:
 	case Instruction::EXTCODEHASH:
 	case Instruction::RETURNDATACOPY: // depends on previous calls
@@ -194,6 +195,7 @@ bool SemanticInformation::movable(Instruction _instruction)
 	{
 	case Instruction::KECCAK256:
 	case Instruction::BALANCE:
+	case Instruction::SELFBALANCE:
 	case Instruction::EXTCODESIZE:
 	case Instruction::EXTCODEHASH:
 	case Instruction::RETURNDATASIZE:
@@ -265,6 +267,7 @@ bool SemanticInformation::invalidInPureFunctions(Instruction _instruction)
 	switch (_instruction)
 	{
 	case Instruction::ADDRESS:
+	case Instruction::SELFBALANCE:
 	case Instruction::BALANCE:
 	case Instruction::ORIGIN:
 	case Instruction::CALLER:
diff --git a/liblangutil/EVMVersion.cpp b/liblangutil/EVMVersion.cpp
index eb87d4832df..3d546a9e4c0 100644
--- a/liblangutil/EVMVersion.cpp
+++ b/liblangutil/EVMVersion.cpp
@@ -40,6 +40,10 @@ bool EVMVersion::hasOpcode(Instruction _opcode) const
 		return hasCreate2();
 	case Instruction::EXTCODEHASH:
 		return hasExtCodeHash();
+	case Instruction::CHAINID:
+		return hasChainID();
+	case Instruction::SELFBALANCE:
+		return hasSelfBalance();
 	default:
 		return true;
 	}
diff --git a/liblangutil/EVMVersion.h b/liblangutil/EVMVersion.h
index 6fb6f7e3e91..3c67e304d58 100644
--- a/liblangutil/EVMVersion.h
+++ b/liblangutil/EVMVersion.h
@@ -84,6 +84,8 @@ class EVMVersion:
 	bool hasBitwiseShifting() const { return *this >= constantinople(); }
 	bool hasCreate2() const { return *this >= constantinople(); }
 	bool hasExtCodeHash() const { return *this >= constantinople(); }
+	bool hasChainID() const { return *this >= istanbul(); }
+	bool hasSelfBalance() const { return *this >= istanbul(); }

 	bool hasOpcode(dev::eth::Instruction _opcode) const;

diff --git a/libyul/AsmAnalysis.cpp b/libyul/AsmAnalysis.cpp
index 2622e413a6d..9c62737d8bc 100644
--- a/libyul/AsmAnalysis.cpp
+++ b/libyul/AsmAnalysis.cpp
@@ -732,6 +732,14 @@ void AsmAnalyzer::warnOnInstructions(dev::eth::Instruction _instr, SourceLocatio
 	{
 		errorForVM("only available for Constantinople-compatible");
 	}
+	else if (_instr == dev::eth::Instruction::CHAINID && !m_evmVersion.hasChainID())
+	{
+		errorForVM("only available for Istanbul-compatible");
+	}
+	else if (_instr == dev::eth::Instruction::SELFBALANCE && !m_evmVersion.hasSelfBalance())
+	{
+		errorForVM("only available for Istanbul-compatible");
+	}
 	else if (
 		_instr == dev::eth::Instruction::JUMP ||
 		_instr == dev::eth::Instruction::JUMPI ||
diff --git a/test/liblll/Compiler.cpp b/test/liblll/Compiler.cpp
index 27db45a5c7e..3e2b3888987 100644
--- a/test/liblll/Compiler.cpp
+++ b/test/liblll/Compiler.cpp
@@ -299,6 +299,8 @@ BOOST_AUTO_TEST_CASE(valid_opcodes_functional)
 		"(NUMBER)",
 		"(DIFFICULTY)",
 		"(GASLIMIT)",
+		"(CHAINID)",
+		"(SELFBALANCE)",
 		"(POP 0)",
 		"(MLOAD 0)",
 		"(MSTORE 0 0)",
diff --git a/test/tools/yulInterpreter/EVMInstructionInterpreter.cpp b/test/tools/yulInterpreter/EVMInstructionInterpreter.cpp
index 45584a19f8f..d6011d8c701 100644
--- a/test/tools/yulInterpreter/EVMInstructionInterpreter.cpp
+++ b/test/tools/yulInterpreter/EVMInstructionInterpreter.cpp
@@ -180,6 +180,8 @@ u256 EVMInstructionInterpreter::eval(
 		return m_state.address;
 	case Instruction::BALANCE:
 		return m_state.balance;
+	case Instruction::SELFBALANCE:
+		return m_state.selfbalance;
 	case Instruction::ORIGIN:
 		return m_state.origin;
 	case Instruction::CALLER:
@@ -208,6 +210,8 @@ u256 EVMInstructionInterpreter::eval(
 		return 0;
 	case Instruction::GASPRICE:
 		return m_state.gasprice;
+	case Instruction::CHAINID:
+		return m_state.chainid;
 	case Instruction::EXTCODESIZE:
 		return u256(keccak256(h256(arg[0]))) & 0xffffff;
 	case Instruction::EXTCODEHASH:
diff --git a/test/tools/yulInterpreter/Interpreter.h b/test/tools/yulInterpreter/Interpreter.h
index bce4928c1ea..04688a1ae00 100644
--- a/test/tools/yulInterpreter/Interpreter.h
+++ b/test/tools/yulInterpreter/Interpreter.h
@@ -70,6 +70,7 @@ struct InterpreterState
 	std::map<dev::h256, dev::h256> storage;
 	dev::u160 address = 0x11111111;
 	dev::u256 balance = 0x22222222;
+	dev::u256 selfbalance = 0x22223333;
 	dev::u160 origin = 0x33333333;
 	dev::u160 caller = 0x44444444;
 	dev::u256 callvalue = 0x55555555;
@@ -81,6 +82,7 @@ struct InterpreterState
 	dev::u256 blockNumber = 1024;
 	dev::u256 difficulty = 0x9999999;
 	dev::u256 gaslimit = 4000000;
+	dev::u256 chainid = 0x01;
 	/// Log of changes / effects. Sholud be structured data in the future.
 	std::vector<std::string> trace;
 	/// This is actually an input parameter that more or less limits the runtime.
```
