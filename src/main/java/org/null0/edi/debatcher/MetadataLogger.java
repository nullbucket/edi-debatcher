package org.null0.edi.debatcher;

// TODO: reverse-engineer metadata logger to decouple
public interface MetadataLogger {

	long logBatchSubmissionData(String transactionId);

	long logIsaData(long batchIdMetadata, String isa13, String isaSegment);

	void updateBatchSubmissionData(long batchIdMetadata, int isaCnt);

	long logEncounter(long batchIdMetadata, long hlIdMetadata, String clm01, String clm05, int lxCnt, Object object,
			String splitEncounterName);

	long logGsData(long isaIdMetadata, String gs06, String gsSegment);

	void updateIsaData(long isaIdMetadata, int gsCnt);

	void updateGsData(long gsIdMetadata, int stCnt);

	long logStData(long gsIdMetadata, String st02, String trim);

	void updateStData(long stIdMetadata, int hlCnt);

	long logHlData(long stIdMetadata, int id, int levelCode, String readField);

}
