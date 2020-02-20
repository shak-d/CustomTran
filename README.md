# CustomTran
Parser for a custom and translator

The grammar recognized by the parser is the following:

     <prog>  ::= <stat> EOF
     
 <statlist>  ::= <stat> <statlistp>
  
<statlistp>  ::= <stat> <statlistp> | ԑ
  
     <stat>  ::= ( <statp> )
     
    <statp>  ::= =ID <expr>
    
               | cond <bexpr> <stat> <elseopt>
               
               | while <bexpr> <stat>
               
               | do <statlist>
               
               | print <exprlist>
               
               | read ID
               
 <elseopt>   ::= (else <stat> ) | ԑ
  
   <bexpr>   ::= ( <bexprp> )
  
  <bexprp>   ::= RELOP <expr> <expr>
  
    <expr>   ::= NUM | ID | ( <exprp> )
    
   <exprp>   ::= + <exprlist> | - <expr> <expr> | * <exprlist> | / <expr> <expr>
  
<exprlist>   ::= <expr> <exprlistp>

<exprlistp>  ::= <expr> <exprlistp> | ԑ
    
  
  
