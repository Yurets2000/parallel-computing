package com.yube.text.processors;

import java.io.File;

public interface TextProcessor {
    String getProcessorName();
    Object processText(File textFile);
}
