package ru.javawebinar.topjava.model;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class UserMealWithExcess {
    private final LocalDateTime dateTime;

    private final String description;

    private final int calories;

    // private final boolean excess;
    private final Supplier<Boolean> excess;

//    public UserMealWithExcess(LocalDateTime dateTime, String description, int calories, boolean excess) {
//        this.dateTime = dateTime;
//        this.description = description;
//        this.calories = calories;
//        this.excess = excess;
//    }

    public UserMealWithExcess(LocalDateTime dateTime, String description, int calories, Supplier<Boolean> excess) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
        this.excess = excess;
    }

    public Boolean getExcess() {
        return excess.get();
    }

    @Override
    public String toString() {
        return "UserMealWithExcess{" +
                "dateTime=" + dateTime +
                ", description='" + description + '\'' +
                ", calories=" + calories +
                //", excess=" + excess +
                ", excess=" + excess.get() +
                '}';
    }
}
