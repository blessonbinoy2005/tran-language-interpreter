# Tran Programming Language Interpreter

This project is an interpreter for the **Tran** programming language, written in Java. It was built as part of an academic project to explore compiler and interpreter design concepts such as lexical analysis, parsing, and execution of custom language constructs.

## Features

- **Lexer**: Tokenizes Tran source code based on defined syntax rules.
- **Parser**: Constructs an abstract syntax tree (AST) from the tokenized input using recursive descent parsing.
- **Interpreter**: Executes the AST, supporting variables, expressions, control structures, user-defined functions, and built-in methods.
- **Built-in Support**: Includes built-in object support (e.g., `console.write()`), with variadic and shared method handling.

## Language Description

Tran is a small, educational programming language designed for learning the fundamentals of programming language implementation. It includes support for:

- Arithmetic and boolean expressions
- Conditional statements
- Loops
- Object definitions
- Function definitions and calls
- Built-in methods

Please see the included **Tran Language Definition** PDF for a full overview of the language's grammar and semantics.

## Grammar

The formal grammar used to define Tran is written in **EBNF (Extended Backus-Naur Form)**. You can find the complete EBNF specification in the file:

