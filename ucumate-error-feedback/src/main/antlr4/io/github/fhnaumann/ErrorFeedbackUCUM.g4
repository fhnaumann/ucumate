grammar ErrorFeedbackUCUM;

mainTerm
    : term EOF # completeMainTerm
    | EOF # emptyMainTerm
    | '/' term # unaryDivTerm
    ;

term
    : component # termOnly
    | term annotation # termWithAnnotation
    | term '{' withinCbSymbol* # termWithAnnotationMissingCurlyClosing
    | term withinCbOrSbSymbol* '}' # termWithAnnotationMissingCurlyOpening
    | annotation # annotationOnly
    // | '{'? withinCbSymbol* '}'? # annotationWithPotentialMissingCurly
    | '{' withinCbOrSbSymbol* # annotationOnlyMissingCurlyClosing
    | withinCbOrSbSymbol* '}' # annotationOnlyMissingCurlyOpening
    | '(' term ')' # parenthesisedTerm
    | '(' term # missingRightParen
    | term ')' # missingLeftParen
    | term '.' term # binaryMulTerm
    | term '/' term # binaryDivTerm
    | term DIV_SYMBOL term # binaryDivTerm
    | term MUL_SYMBOL term # binaryMulTerm
    | ('.' | '/' | MUL_SYMBOL | DIV_SYMBOL) term # missingLHS
    | term ('.' | '/' | MUL_SYMBOL | DIV_SYMBOL) # missingRHS
    ;

incompleteTerm
    : term
    ;

MUL_SYMBOL : '*' | '×' | '✕' | '✖' | '⨯';
DIV_SYMBOL : '÷' | '∕' | '➗';

component
    : simpleSymbolUnit # componentOnly
    | simpleSymbolUnit EXPONENT_SYMBOL? exponent # componentWithExponent
    ;

EXPONENT_SYMBOL: '^';

annotation
    : '{' withinCbSymbol* '}'
    ;

dimlessUnit
    : '10*' // # numberTenForArbitraryPowersWithAsteriks
    | '10^' // # numberTenForArbitraryPowersWithCircumflex
    | '[pi]' // # numberPi
    | '%' // # percent
    | '[ppth]' // # partsPerThousand
    | '[ppm]' // # partsPerMillion
    | '[ppb]' // # partsPerBillion
    | '[pptr]' // # partsPerTrillion
    ;

terminalSymbolUnit
    : NON_DIGIT_TERMINAL_UNIT_SYMBOL
    | DIGIT_SYMBOL
    ;

simpleSymbolUnit
    : digitSymbols # numberUnit // i.e. "5", "56"
    | invalidDigitSymbols # invalidNumberUnit // i.e. "-5"
    | maybeAPrefix? dimlessUnit # maybeAPrefixSymbolUnit // i.e. "10*", "%"
    | maybePrefixSymbolUnit # maybeAPrefixSymbolUnit // i.e. "m", "cm", "notavalidprefix"
    | '[' withinSbSymbol+ ']' # stigmatizedSymbolUnit
    | maybeAPrefix? '[' withinSbSymbol+ # stigmatizedSymbolUnitMissingClosingSquareBracket
    | maybeAPrefix? withinSbSymbol+ ']' # stigmatizedSymbolunitMissingOpeningSquareBracket
    | maybeAPrefix '[' withinSbSymbol+ ']' # maybeAPrefixSymbolUnit // i.e. "[lb_av]", "c[lb_av]", per definition prefixing non-metric is not allowed, but most parsers allow it
    | maybeAPrefix? 'g%' # maybeAPrefixSymbolUnit // 'g%' requires special handling because the '%' is part of the symbol in the unit and not the dimless '%' unit
    | maybeAPrefix? '%[slope]' #maybeAPrefixSymbolUnit // '%[slope]' requires special handling because the '%' is part of the symbol in the unit and not the dimless '%' unit'
    ;

maybeAPrefix
    : NON_DIGIT_TERMINAL_UNIT_SYMBOL+ // deka ("da") is the only prefix with two symbols...
    ;

maybePrefixSymbolUnit
    : NON_DIGIT_TERMINAL_UNIT_SYMBOL+
    ;

exponent
    : ('+' | '-') digitSymbols # exponentWithExplicitSign
    | digitSymbols # exponentWithoutSign
    ;

invalidDigitSymbols
    : '-' digitSymbols
    ;

digitSymbols
    : (DIGIT_SYMBOL)+
    ;


withinSbSymbol
    : withinCbOrSbSymbol
    | '{'
    | '}'
    ;

withinCbSymbol
    : withinCbOrSbSymbol
    // | ' ' test commented out
    | '['
    | ']'
    ;

withinCbOrSbSymbol
    : terminalSymbolUnit
    | '"'
    | '('
    | ')'
    | '+'
    | '-'
    | '.'
    | '/'
    | '='
    ;

NON_DIGIT_TERMINAL_UNIT_SYMBOL
    : '!'
    | '#'
    | '$'
    | '%'
    | '&'
    | '\''
    | '*'
    | ','
    | ':'
    | ';'
    | '<'
    | '>'
    | '?'
    | '@'
    | 'A' .. 'Z'
    | '\\'
    | '^'
    | '_'
    | '`'
    | 'a' .. 'z'
    | '|'
    | '~'
    ;

DIGIT_SYMBOL
    : '0' .. '9'
    ;

WS : [ \t\r\n]+ -> channel(HIDDEN);