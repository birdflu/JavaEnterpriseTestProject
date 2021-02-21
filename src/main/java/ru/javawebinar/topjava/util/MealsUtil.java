package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

public class MealsUtil {
  public static void main(String[] args) {
    List<Meal> meals = Arrays.asList(
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
    );

    System.out.println("\nfilteredByCycles");
    List<MealTo> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
    mealsTo.forEach(System.out::println);

    System.out.println("\nfilteredByStreams");
    System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));

    System.out.println("\nfastFilteredByCycles");
    fastFilteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
            .forEach(System.out::println);

    System.out.println("\nfastFilteredByPredicate");
    fastFilteredByPredicate(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
            .forEach(System.out::println);

    System.out.println("\nfastFilteredByConsumer");
    fastFilteredByConsumer(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
            .forEach(System.out::println);

    System.out.println("\nfastFilteredByStreams");
    System.out.println(fastFilteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));

  }

  public static List<MealTo> filteredByCycles(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    final Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    for (Meal meal : meals) {
      summarizingCalories.put(meal.getDate(),
              summarizingCalories.getOrDefault(meal.getDate(), 0) +
                      meal.getCalories());
    }

    List<MealTo> filteredMeals = new ArrayList<>();

    for (Meal meal : meals) {
      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        filteredMeals.add(getMealTo(caloriesPerDay, summarizingCalories, meal, meal.getDateTime().toLocalDate()));
      }
    }
    return filteredMeals;
  }

  public static List<MealTo> filteredByStreams(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories =
            meals.stream().collect(groupingBy(Meal::getDate,
                    summingInt(Meal::getCalories)));
    return meals.stream()
            .filter(m -> TimeUtil.isBetweenHalfOpen(m.getTime(), startTime, endTime))
            .map(m -> getMealTo(caloriesPerDay, summarizingCalories, m, m.getDateTime().toLocalDate()))
            .collect(toList());
  }

  public static List<MealTo> fastFilteredByStreams(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    return meals.stream().collect(groupingBy(Meal::getDate)).entrySet().stream().flatMap(Meals -> {
      Map<LocalDate, Integer> summarizingCalories = Meals.getValue().stream().collect(groupingBy(Meal::getDate,
              summingInt(Meal::getCalories)));
      return Meals.getValue().stream()
              .filter(m -> TimeUtil.isBetweenHalfOpen(m.getTime(), startTime, endTime))
              .map(m -> getMealTo(caloriesPerDay, summarizingCalories, m, m.getDateTime().toLocalDate()));
    }).collect(toList());
  }

  public static List<MealTo> fastFilteredByCycles(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    List<Meal> filteredMeals = new ArrayList<>();

    for (Meal meal : meals) {
      summarizingCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum);

      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        filteredMeals.add(meal);
      }
    }

    List<MealTo> MealToes = new ArrayList<>();

    for (Meal meal : filteredMeals) {
      MealToes.add(getMealTo(caloriesPerDay, summarizingCalories, meal, meal.getDate()));
    }

    return MealToes;
  }

  public static List<MealTo> fastFilteredByPredicate(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    List<MealTo> filteredMeals = new ArrayList<>();

    Predicate<Boolean> predicate = b -> true;

    for (Meal meal : meals) {
      summarizingCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum);

      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        predicate = predicate.and(b -> filteredMeals.add(getMealTo(
                caloriesPerDay, summarizingCalories, meal, meal.getDate())));
      }
    }
    predicate.test(true);

    return filteredMeals;
  }

  public static List<MealTo> fastFilteredByConsumer(
          List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
    Map<LocalDate, Integer> summarizingCalories = new HashMap<>();
    List<MealTo> filteredMeals = new ArrayList<>();

    Consumer<Void> consumer = c -> {
    };

    for (Meal meal : meals) {
      summarizingCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum);

      if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
        consumer = consumer.andThen(c -> filteredMeals.add(getMealTo(
                caloriesPerDay, summarizingCalories, meal, meal.getDate())));
      }
    }
    consumer.accept(null);

    return filteredMeals;
  }

  private static MealTo getMealTo(
          int caloriesPerDay,
          Map<LocalDate, Integer> summarizingCalories,
          Meal m,
          LocalDate localDate) {
    return new MealTo(m.getDateTime(), m.getDescription(), m.getCalories(),
            caloriesPerDay < summarizingCalories.getOrDefault(localDate, 0));
  }

}
