package org.null0.x12.debatcher;

// TODO from UCDetector: Class "DefaultMetadata" is only called from tests
public class DefaultMetadata implements Metadata { // NO_UCD (test only)
	@Override
	public long logBatchSubmissionData(String transactionId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long logClaim(long batchIdMetadata, long hlIdMetadata, String clm01, String clm05, int lxCnt, Object object, String claimName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long logGsData(long isaIdMetadata, String gs06, String gsSegment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long logHlData(long stIdMetadata, int id, int levelCode, String readField) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long logIsaData(long batchIdMetadata, String isa13, String isaSegment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long logStData(long gsIdMetadata, String st02, String trim) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateBatchSubmissionData(long batchIdMetadata, int isaCnt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateGsData(long gsIdMetadata, int stCnt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateIsaData(long isaIdMetadata, int gsCnt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStData(long stIdMetadata, int hlCnt) {
		// TODO Auto-generated method stub
	}
}
