package com.bobrust.unit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bobrust.robot.ButtonConfiguration;
import org.junit.jupiter.api.Test;

public class ButtonConfigurationTest {
	
	@Test
	public void test() {
		ButtonConfiguration config = new ButtonConfiguration();
		System.out.println(config.serialize());
	}
}
