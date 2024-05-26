import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int THREAD_POOL_SIZE = 100; // 控制线程池大小

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader("./hash.txt"));
             BufferedReader dictReader = new BufferedReader(new FileReader("./dict.txt"))) {
            HashSet<String> dictionary = loadDictionary(dictReader);
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(":");
                String username = columns[0];
                String hash = columns[1];
                String salt = columns[2];
                executor.execute(new PasswordCrackerTask(username, hash, salt, dictionary));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                // 等待所有线程完成
            }
        } catch (IOException e) {
            System.err.println("发生IO异常: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("总运行时间: " + totalTime + " 毫秒");
    }

    private static HashSet<String> loadDictionary(BufferedReader reader) throws IOException {
        HashSet<String> dictionary = new HashSet<>();
        String password;
        while ((password = reader.readLine()) != null) {
            dictionary.add(password);
        }
        return dictionary;
    }

    static class PasswordCrackerTask implements Runnable {
        private final String username;
        private final String hash;
        private final String salt;
        private final HashSet<String> dictionary;

        public PasswordCrackerTask(String username, String hash, String salt, HashSet<String> dictionary) {
            this.username = username;
            this.hash = hash;
            this.salt = salt;
            this.dictionary = dictionary;
        }

        @Override
        public void run() {
            for (String password : dictionary) {
                if (PasswordUtil.decrypt(hash, password, salt).equals(username)) {
                    System.out.println("Username: " + username + ", Password: " + password + ", Hash: " + hash + ", Salt: " + salt);
                    return;
                }
            }
        }
    }
}
