package com.atlassian.theplugin.util;

import com.google.common.collect.ImmutableSet;

import java.util.Locale;
import java.util.Set;

/**
 * Created by klopacinski on 2015-01-28.
 */
public class JQLUtil {
    public static final Set<String> JQL_RESERVED_WORDS;

    static {
        JQL_RESERVED_WORDS = ImmutableSet.of("abort", "access", "add", "after", "alias", "all", "alter", "and", "any", "as", "asc",
                "audit", "avg", "before", "begin", "between", "boolean", "break", "by", "byte", "catch", "cf", "changed",
                "char", "character", "check", "checkpoint", "collate", "collation", "column", "commit", "connect", "continue",
                "count", "create", "current", "date", "decimal", "declare", "decrement", "default", "defaults", "define", "delete",
                "delimiter", "desc", "difference", "distinct", "divide", "do", "double", "drop", "else", "empty", "encoding",
                "end", "equals", "escape", "exclusive", "exec", "execute", "exists", "explain", "false", "fetch", "file", "field",
                "first", "float", "for", "from", "function", "go", "goto", "grant", "greater", "group", "having",
                "identified", "if", "immediate", "in", "increment", "index", "initial", "inner", "inout", "input", "insert",
                "int", "integer", "intersect", "intersection", "into", "is", "isempty", "isnull", "join", "last", "left",
                "less", "like", "limit", "lock", "long", "max", "min", "minus", "mode", "modify",
                "modulo", "more", "multiply", "next", "noaudit", "not", "notin", "nowait", "null", "number", "object",
                "of", "on", "option", "or", "order", "outer", "output", "power", "previous", "prior", "privileges",
                "public", "raise", "raw", "remainder", "rename", "resource", "return", "returns", "revoke", "right", "row",
                "rowid", "rownum", "rows", "select", "session", "set", "share", "size", "sqrt", "start", "strict",
                "string", "subtract", "sum", "synonym", "table", "then", "to", "trans", "transaction", "trigger", "true",
                "uid", "union", "unique", "update", "user", "validate", "values", "view", "was", "when", "whenever", "where",
                "while", "with");
    }

    // note that this only escapes single words - it's a simple bandaid to a problem, not a full-blown JQL parser
    public static String escapeReservedJQLKeyword(String text) {
        if (JQL_RESERVED_WORDS.contains(text.toLowerCase(Locale.ENGLISH))) {
            return "\"" + text + "\"";
        }
        return text;
    }
}
