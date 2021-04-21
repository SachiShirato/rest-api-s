package jp.co.acom.fehub.xml;

import lombok.Getter;

public enum TsAttribute {

	KBN("@KBN"),

	LVL("@LVL"),

	SVR("@SVR"),

	SVC("@SVC");

	@Getter
	private String tName;

	private TsAttribute(String tName) {
		this.tName = tName;
	}
}
