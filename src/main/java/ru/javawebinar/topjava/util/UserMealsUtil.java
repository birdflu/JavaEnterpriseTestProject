package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class UserMealsUtil {
  public static void main(String[] args) {
    List<UserMeal> meals = Arrays.asList(
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
            new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
    );

    List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
    mealsTo.forEach(System.out::println);

    System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));

    fastFilteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
            .forEach(System.out::println);

  }

  public static List<UserMealWithExcess> filteredByCycles(
          List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    for (UserMeal meal : meals) {
      summarizingCalories.put(meal.getDate(),
              summarizingCalories.getOrDefault(meal.getDate(), 0) +
                      meal.getCalories());
    }

    List<UserMealWithExcess> filteredMeals = new ArrayList<>();

    for (UserMeal meal : meals) {
      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        filteredMeals.add(getUserMealWithExcess(caloriesPerDay, summarizingCalories, meal, meal.getDateTime().toLocalDate()));
      }
    }
    return filteredMeals;
  }

  public static List<UserMealWithExcess> filteredByStreams(
          List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories =
            meals.stream().collect(groupingBy(u -> u.getDateTime().toLocalDate(),
                    Collectors.summingInt(UserMeal::getCalories)));
    return meals.stream()
            .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
            .map(m -> getUserMealWithExcess(caloriesPerDay, summarizingCalories, m, m.getDateTime().toLocalDate()))
            .collect(Collectors.toList());
  }

  public static List<UserMealWithExcess> fastFilteredByCycles(
          List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    List<UserMeal> filteredMeals = new ArrayList<>();

    for (UserMeal meal : meals) {
      summarizingCalories.put(meal.getDate(),
              summarizingCalories.getOrDefault(meal.getDate(), 0) +
                      meal.getCalories());

      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        filteredMeals.add(meal);
      }
    }

    List<UserMealWithExcess> userMealWithExcesses = new ArrayList<>();

    for (UserMeal meal : filteredMeals) {
      userMealWithExcesses.add(getUserMealWithExcess(caloriesPerDay, summarizingCalories, meal, meal.getDate()));
    }

    return userMealWithExcesses;
  }

  private static UserMealWithExcess getUserMealWithExcess(int caloriesPerDay, Map<LocalDate, Integer> summarizingCalories, UserMeal m, LocalDate localDate) {
    return new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
            caloriesPerDay < summarizingCalories.getOrDefault(localDate, 0));
  }

}
