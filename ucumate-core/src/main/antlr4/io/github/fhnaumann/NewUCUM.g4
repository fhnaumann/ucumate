grammar NewUCUM;

mainTerm
    : term EOF # completeMainTerm
    | '/' term # unaryDivTerm
    ;

term
    : component # termOnly
    | term annotation # termWithAnnotation
    | annotation # annotationOnly
    | '(' term ')' # parenthesisedTerm
    | term '/' term # binaryDivTerm
    | term '.' term # binaryMulTerm
    ;

component
    : simpleSymbolUnit # componentOnly
    | simpleSymbolUnit exponent # componentWithExponent
    ;

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
    | maybeAPrefix? dimlessUnit # maybeAPrefixSymbolUnit // i.e. "10*", "%"
    | maybePrefixSymbolUnit # maybeAPrefixSymbolUnit // i.e. "m", "cm", "notavalidprefix"
    | '[' withinSbSymbol+ ']' # stigmatizedSymbolUnit
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
    | ' '
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