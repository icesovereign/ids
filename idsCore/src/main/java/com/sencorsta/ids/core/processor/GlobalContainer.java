package com.sencorsta.ids.core.processor;

import com.sencorsta.ids.core.entity.Client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ICe
 */
public class GlobalContainer {
    public static final Map<String, Client> CLIENTS = new ConcurrentHashMap<>();
}
