package com.example.mq;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

public enum QUEUE {

	QA_DH_DF("QA.DH.DF"),

	QA_DH_DL("QA.DH.DL"),

	QC_DH_REQ("QC.DH.REQ"),

	QL_DH_ERR("QL.DH.ERR"),

	QL_DH_HTTP_LSR("QL.DH.HTTP_LSR"),

	QL_DH_REP("QL.DH.REP"),

	QL_DH_REQ("QL.DH.REQ"),

	QL_DW_REP("QL.DW.REP"),

	SYSTEM_ADMIN_EVENT("SYSTEM.ADMIN.EVENT");

	@Getter
	private String qName;

	private QUEUE(String qName) {
		this.qName = qName;
	}

	public static void print() {
		Stream.of(values()).forEach(q -> System.out.println(q.getQName()));
	}

	public static List<String> getList() {

		return Stream.of(values()).map(q -> q.getQName()).collect(Collectors.toList());
	}
}
