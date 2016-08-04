package com.skogul.spring.acl.domain.predicate;

import java.util.List;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.skogul.spring.acl.domain.jpa.AclClass;
import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;
import com.skogul.spring.acl.domain.jpa.AclSid;
import com.skogul.spring.acl.domain.jpa.QAclClass;
import com.skogul.spring.acl.domain.jpa.QAclEntry;
import com.skogul.spring.acl.domain.jpa.QAclObjectIdentity;
import com.skogul.spring.acl.domain.jpa.QAclSid;

public abstract class AclPredicate<T, Q extends EntityPathBase<T>> {

	protected abstract Class<T> getType();

	protected abstract NumberPath<Long> getObjectId();

	protected abstract Q getInnerObject();

	protected abstract NumberPath<Long> getInnerObjectId();

	private SubQueryExpression<AclSid> selectAclSidSubQuery(List<String> sids) {
		QAclSid aclSid = QAclSid.aclSid;
		return JPAExpressions.selectFrom(aclSid).where(aclSid.sid.in(sids));
	}

	private SubQueryExpression<AclClass> selectAclClassSubQuery() {
		QAclClass aclClass = QAclClass.aclClass;
		return JPAExpressions.selectDistinct(aclClass).from(aclClass)
				.where(aclClass.className.eq(getType().getName()));
	}

	private SubQueryExpression<AclObjectIdentity> selectAclObjectIdentity(
			NumberPath<Long> id) {
		QAclObjectIdentity objectIdentity = QAclObjectIdentity.aclObjectIdentity;
		return JPAExpressions
				.selectDistinct(objectIdentity)
				.from(objectIdentity)
				.where(objectIdentity.objectIdIdentity.eq(id).and(
						objectIdentity.objectIdClass
								.eq(selectAclClassSubQuery())));
	}

	private BooleanExpression selectCountOfAclEntry(NumberPath<Long> id,
			List<String> sids) {
		QAclEntry aclEntry = QAclEntry.aclEntry;
		return JPAExpressions
				.select(Wildcard.count)
				.from(aclEntry)
				.where(aclEntry.aclObjectIdentity.eq(
						selectAclObjectIdentity(id)).and(
						aclEntry.sid.in(selectAclSidSubQuery(sids)))).gt(0L);
	}

	private SubQueryExpression<Long> selectWhereSomeObjectHasAnAclEntry(
			NumberPath<Long> id, List<String> sids) {
		// QCustomer innerSomeObject = new QCustomer("innerSomeObject");
		return JPAExpressions.select(getInnerObjectId()).from(getInnerObject())
				.where(selectCountOfAclEntry(id, sids));
	}

	/**
	 * Returns a predicate defining a query from {@code SomeObject} which
	 * performs the necessary check to ensure that the user has privileges to
	 * view the someObject in at least some form or another. This predicate is
	 * used for non global roles to determine if someObject should be displayed
	 * on the dashboard.
	 * 
	 * Runs a sub-query to determine all the id's that the user has permission
	 * to view and then does a simple check to figure out whether the current id
	 * is present in the set of viewable ids.
	 * 
	 * @param sids
	 *            - the sids for the user. These can be obtained from the
	 *            AuthUtil
	 * @return - the predicate
	 */
	public BooleanExpression viewableFor(List<String> sids) {
		return getObjectId().in(
				selectWhereSomeObjectHasAnAclEntry(getObjectId(), sids));
	}
}
