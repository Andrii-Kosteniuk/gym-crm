package com.training.gym.util;

import java.util.function.Predicate;

public interface UsernameGenerator {

    String generate(String firstName, String lastName, Predicate<String> usernameExists);
}
