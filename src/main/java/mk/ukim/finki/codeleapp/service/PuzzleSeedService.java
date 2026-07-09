package mk.ukim.finki.codeleapp.service;

import java.time.LocalDate;
import java.util.Set;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Difficulty;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.repository.PuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts built-in puzzles when missing. Safe to call on every startup and on first HTTP access
 * if the table was empty (e.g. runner order, cleared DB, or devtools edge cases).
 */
@Service
public class PuzzleSeedService {

    private final Object seedLock = new Object();
    private final PuzzleRepository puzzleRepository;

    public PuzzleSeedService(PuzzleRepository puzzleRepository) {
        this.puzzleRepository = puzzleRepository;
    }

    /**
     * Fills an empty {@code puzzles} table. Cheap no-op when rows already exist.
     */
    @Transactional
    public void ensureDefaultPuzzles() {
        if (puzzleRepository.count() > 0) {
            return;
        }
        synchronized (seedLock) {
            if (puzzleRepository.count() > 0) {
                return;
            }
            insertAllDefaults();
        }
    }

    private void insertAllDefaults() {
        LocalDate today = LocalDate.now();

        saveIfMissing(puzzle("bubble-sort", "BubbleSort", "Java", Category.ALGORITHMS, Difficulty.EASY,
            """
            public static void bubbleSort(int[] arr) {
                for (int i = 0; i < arr.length - 1; i++) {
                    for (int j = 0; j < arr.length - i - 1; j++) {
                        if (arr[j] > arr[j + 1]) {
                            int temp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = temp;
                        }
                    }
                }
            }
            """, "Classic adjacent swap sorting algorithm.", "Compares and swaps neighboring elements repeatedly.",
            Set.of("sorting", "arrays"), today));

        saveIfMissing(puzzle("binary-search", "BinarySearch", "Java", Category.ALGORITHMS, Difficulty.MEDIUM,
            """
            public static int binarySearch(int[] arr, int target) {
                int left = 0, right = arr.length - 1;
                while (left <= right) {
                    int mid = left + (right - left) / 2;
                    if (arr[mid] == target) return mid;
                    if (arr[mid] < target) left = mid + 1;
                    else right = mid - 1;
                }
                return -1;
            }
            """, "Efficient lookup in sorted arrays.", "Halves search space each iteration.",
            Set.of("search", "logn"), today.plusDays(1)));

        saveIfMissing(puzzle("fizzbuzz", "FizzBuzz", "JavaScript", Category.BEGINNER_EXERCISES, Difficulty.EASY,
            """
            function fizzBuzz(n) {
              for (let i = 1; i <= n; i++) {
                if (i % 15 === 0) console.log("FizzBuzz");
                else if (i % 3 === 0) console.log("Fizz");
                else if (i % 5 === 0) console.log("Buzz");
                else console.log(i);
              }
            }
            """, "Prints labels for multiples.", "Conditionally prints Fizz/Buzz labels.",
            Set.of("loops", "mod"), today.plusDays(2)));

        saveIfMissing(puzzle("palindrome-checker", "PalindromeChecker", "Python", Category.UTILITY_SCRIPTS, Difficulty.EASY,
            """
            def is_palindrome(text: str) -> bool:
                cleaned = ''.join(ch.lower() for ch in text if ch.isalnum())
                return cleaned == cleaned[::-1]
            """, "Checks mirrored string equality.", "Normalizes string and compares against reverse.",
            Set.of("string", "cleaning"), today.minusDays(4)));

        saveIfMissing(puzzle("two-sum", "TwoSum", "Java", Category.REAL_WORLD_FUNCTIONS, Difficulty.MEDIUM,
            """
            public int[] twoSum(int[] nums, int target) {
                Map<Integer, Integer> indexByValue = new HashMap<>();
                for (int i = 0; i < nums.length; i++) {
                    int need = target - nums[i];
                    if (indexByValue.containsKey(need)) {
                        return new int[]{indexByValue.get(need), i};
                    }
                    indexByValue.put(nums[i], i);
                }
                return new int[]{-1, -1};
            }
            """, "Finds pair whose sum matches target.", "Uses hash map for O(n) lookup.",
            Set.of("hashmap", "arrays"), today.minusDays(3)));

        saveIfMissing(puzzle("stack-impl", "StackImplementation", "Java", Category.DATA_STRUCTURES, Difficulty.MEDIUM,
            """
            class Stack {
                private final List<Integer> data = new ArrayList<>();
                public void push(int value) { data.add(value); }
                public int pop() { return data.remove(data.size() - 1); }
                public int peek() { return data.get(data.size() - 1); }
                public boolean isEmpty() { return data.isEmpty(); }
            }
            """, "LIFO container operations.", "Supports push/pop/peek/isEmpty.",
            Set.of("lifo", "collections"), today.minusDays(2)));

        saveIfMissing(puzzle("factorial", "Factorial", "Python", Category.BEGINNER_EXERCISES, Difficulty.EASY,
            """
            def factorial(n: int) -> int:
                if n <= 1:
                    return 1
                return n * factorial(n - 1)
            """, "Multiplies down to one.", "Classic recursive definition of n!.",
            Set.of("recursion", "math"), today.minusDays(1)));

        saveIfMissing(puzzle("fibonacci", "Fibonacci", "JavaScript", Category.ALGORITHMS, Difficulty.EASY,
            """
            function fibonacci(n) {
              if (n <= 1) return n;
              let a = 0, b = 1;
              for (let i = 2; i <= n; i++) {
                const next = a + b;
                a = b;
                b = next;
              }
              return b;
            }
            """, "Each term is the sum of the two before it.", "Iteratively builds the sequence.",
            Set.of("loops", "sequence"), today.plusDays(3)));

        saveIfMissing(puzzle("reverse-string", "ReverseString", "Java", Category.UTILITY_SCRIPTS, Difficulty.EASY,
            """
            public static String reverse(String input) {
                char[] chars = input.toCharArray();
                int left = 0, right = chars.length - 1;
                while (left < right) {
                    char temp = chars[left];
                    chars[left++] = chars[right];
                    chars[right--] = temp;
                }
                return new String(chars);
            }
            """, "Flips characters in place.", "Two-pointer swap from both ends.",
            Set.of("string", "two-pointer"), today.plusDays(4)));

        saveIfMissing(puzzle("linked-list-reverse", "ReverseLinkedList", "Java", Category.DATA_STRUCTURES, Difficulty.MEDIUM,
            """
            public ListNode reverseList(ListNode head) {
                ListNode prev = null;
                ListNode current = head;
                while (current != null) {
                    ListNode next = current.next;
                    current.next = prev;
                    prev = current;
                    current = next;
                }
                return prev;
            }
            """, "Walks nodes and flips next pointers.", "Iterative in-place list reversal.",
            Set.of("linked-list", "pointers"), today.plusDays(5)));
    }

    private void saveIfMissing(Puzzle candidate) {
        if (puzzleRepository.findBySlug(candidate.getSlug()).isEmpty()) {
            puzzleRepository.save(candidate);
        }
    }

    private Puzzle puzzle(
        String slug, String answer, String language, Category category, Difficulty difficulty, String code,
        String clue, String explanation, Set<String> tags, LocalDate dailyDate
    ) {
        Puzzle p = new Puzzle();
        p.setSlug(slug);
        p.setAnswer(answer);
        p.setLanguage(language);
        p.setCategory(category);
        p.setDifficulty(difficulty);
        p.setCodeContent(code.strip());
        p.setShortClue(clue);
        p.setExplanation(explanation);
        p.setTags(tags);
        p.setScheduledDate(dailyDate);
        return p;
    }
}
