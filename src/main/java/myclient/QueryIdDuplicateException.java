package myclient;

class QueryIdDuplicateException extends RuntimeException {
    public QueryIdDuplicateException(int queryId) {
        super(String.format("QueryId %s duplicate", queryId));
    }
}
