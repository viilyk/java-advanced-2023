package info.kgeorgiy.ja.ilyk.student;

import info.kgeorgiy.java.advanced.student.*;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }
    private static final Comparator<Student> BY_NAME = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparing(Student::getId);

    private <T> List<T> mapList(Collection<Student> students, Function<Student, T> f) {
        return students.stream().map(f).collect(Collectors.toList());
    }

    private <T> Stream<Student> find(Collection<Student> students, Function<Student, T> f, T v) {
        return students.stream().filter(u -> f.apply(u).equals(v)).sorted(BY_NAME);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapList(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted().toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(BY_NAME).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return find(students, Student::getFirstName, name).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return find(students, Student::getLastName, name).toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return find(students, Student::getGroup, group).toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return find(students, Student::getGroup, group).collect(Collectors.toMap(Student::getLastName,
                Student::getFirstName,
                BinaryOperator.minBy(Comparator.naturalOrder())));
    }

}
