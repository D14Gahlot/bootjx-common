package com.boot.jx.postman.contack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;

@Component
public class ContackService {

	@Autowired
	MongoTemplate mongoTemplate;

	public void pushContactUser(MobileUserDoc safeUserDoc, MobileUserContactDoc safeContactDoc) {
		if (ArgUtil.nullAsFalse(safeUserDoc.getPositive())) {
			MobileUserDoc safeContactUserDoc = mongoTemplate.findById(safeContactDoc.getMobile(), MobileUserDoc.class);
			if (ArgUtil.is(safeContactUserDoc)) {
				switch (safeContactDoc.getRelation()) {
				case NOCONTACT:
					break;
				case HOUSEMATE:
				case NEARBY:
				case REGULAR:
				case CONTACT:
				default:
					safeContactUserDoc.setSyncRequired(true);
					mongoTemplate.save(safeContactUserDoc);
					break;
				}
			}
		}
	}

	public void pullContactUpdate(MobileUserDoc safeUserDoc, MobileUserContactDTO safeContactDoc) {
		MobileUserDoc safeContactUserDoc = mongoTemplate.findById(safeContactDoc.getMobile(), MobileUserDoc.class);
		if (ArgUtil.is(safeContactUserDoc)) {
			safeContactDoc.setScore(safeContactUserDoc.getScore());
			Integer myOldScore = safeUserDoc.getScore();
			if (!ArgUtil.is(myOldScore)) {
				myOldScore = 0;
			}
			Integer myNewScore = myOldScore;
			Integer contactScore = safeContactDoc.getScore();
			if (!ArgUtil.is(contactScore)) {
				contactScore = 0;
			}

			if (ArgUtil.is(safeContactUserDoc)) {
				switch (safeContactDoc.getRelation()) {
				case NOCONTACT:
					break;
				case HOUSEMATE:
					myNewScore = contactScore + 1;
					break;
				case NEARBY:
					myNewScore = contactScore + 3;
					break;
				case CONTACT:
					myNewScore = contactScore + 5;
					break;
				case REGULAR:
					myNewScore = contactScore + 10;
					break;
				default:
					myNewScore = contactScore + 15;
					break;
				}
				safeUserDoc.setScore(Math.min(myOldScore, myNewScore));
			}
		}

	}

}
