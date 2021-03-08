package com.dreamgyf.asm.plugin;

import java.io.File;
import java.util.List;

public class AsmConfigModel {
	/**
	 * 规则: 全限定类名开头
	 */
	public String patternStartWith;

	public List<String> excludes;
	public File excludeBy;
}
