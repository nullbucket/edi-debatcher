package org.null0.edi.debatcher.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtility {
	private static final Logger logger = LoggerFactory.getLogger(TestUtility.class);

	public static void main(String[] args) {

		// StringBuffer clmString = new
		// StringBuffer("CLM*2997677856479709654A*300***12:B:1*Y*A*Y*Y~CN1*09*240~HI*BK:78901~NM1*82*1*JONES*JIM****XX*99875632141~PRV*PE*PXC*152W0000X~SBR*P*18*XYZ1234567******ZZ~CAS*CO*97*60~AMT*D*240~AMT*EAF*0~OI***Y***Y~NM1*IL*1*MASSEY*ABIGAIL****MI*897148306~N3*101
		// CAMBRIDGE MANOR~N4*NORFOLK*VA*235099999~NM1*PR*2*HAPPY HEALTH
		// PLAN*****XV*H9999~N3*705 E HUGH
		// ST~N4*NORFOLK*VA*235049999~LX*1~SV1*HC:T2029*100*UN*1***1~DTP*472*D8*20150401~SVD*H9999*80*HC:T2029**1~CAS*CO*97*20~DTP*573*D8*20150403~AMT*EAF*0~LX*2~SV1*HC:S9125*100*UN*1***1~DTP*472*D8*20150401~SVD*H9999*80*HC:S9125**1~CAS*CO*97*20~DTP*573*D8*20150403~AMT*EAF*0~LX*3~SV1*HC:S9122*100*UN*1***1~DTP*509*D8*20150401~SVD*H9999*80*HC:S9122**1~CAS*CO*97*20~DTP*573*D8*20150403~");
		StringBuffer clmString = new StringBuffer(
				"CLM*2997677856479709654A*300***12:B:1*Y*A*Y*Y~CN1*09*240~REF*D9*HELLO~HI*BK:78901~NM1*82*1*JONES*JIM****XX*99875632141~PRV*PE*PXC*152W0000X~SBR*P*18*XYZ1234567******ZZ~CAS*CO*97*60~AMT*D*240~AMT*EAF*0~OI***Y***Y~NM1*IL*1*MASSEY*ABIGAIL****MI*897148306~N3*101 CAMBRIDGE MANOR~N4*NORFOLK*VA*235099999~NM1*PR*2*HAPPY HEALTH PLAN*****XV*H9999~N3*705 E HUGH ST~N4*NORFOLK*VA*235049999~LX*1~SV1*HC:T2029*100*UN*1***1~DTP*472*D8*20150401~SVD*H9999*80*HC:T2029**1~CAS*CO*97*20~DTP*573*D8*20150403~AMT*EAF*0~LX*2~SV1*HC:S9125*100*UN*1***1~DTP*472*D8*20150401~SVD*H9999*80*HC:S9125**1~CAS*CO*97*20~DTP*573*D8*20150403~AMT*EAF*0~LX*3~SV1*HC:S9122*100*UN*1***1~DTP*509*D8*20150401~SVD*H9999*80*HC:S9122**1~CAS*CO*97*20~DTP*573*D8*20150403~");

		String searchRefD9 = "REF*D9*";
		String newRefD9 = "REF*D9*TRANSID_1";

		int refD9StartsAt = clmString.indexOf(searchRefD9);
		if (refD9StartsAt >= 0) // refD9 exists replace it
		{
			int refD9EndsAt = clmString.indexOf("~", refD9StartsAt);
			String existingRefD9 = clmString.substring(refD9StartsAt, refD9EndsAt);
			clmString.replace(refD9StartsAt, refD9EndsAt, newRefD9);
		} else // doesn't exist, insert it
		{
			int hiStartsAt = clmString.indexOf("~HI*");
			clmString.insert(hiStartsAt + 1, newRefD9 + "~");
		}

		logger.info(clmString.toString());

	}

}
