package jp.co.acom.fehub.xml;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static List<String> getList() {
		return Stream.of(values()).map(q -> q.getTName()).collect(Collectors.toList());
	}
}
