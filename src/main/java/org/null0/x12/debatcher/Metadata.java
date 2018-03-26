package org.null0.x12.debatcher;

// TODO: reverse-engineer metadata logger to decouple
public interface Metadata {
	long logBatchSubmissionData(String transactionId);
	long logClaim(long batchIdMetadata, long hlIdMetadata, String clm01, String clm05, int lxCnt, Object object, String claimName);
	long logGsData(long isaIdMetadata, String gs06, String gsSegment);
	long logHlData(long stIdMetadata, int id, int levelCode, String readField);
	long logIsaData(long batchIdMetadata, String isa13, String isaSegment);
	long logStData(long gsIdMetadata, String st02, String trim);
	void updateBatchSubmissionData(long batchIdMetadata, int isaCnt);
	void updateGsData(long gsIdMetadata, int stCnt);
	void updateIsaData(long isaIdMetadata, int gsCnt);
	void updateStData(long stIdMetadata, int hlCnt);
}
