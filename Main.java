public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: <totalCount> <threads>");
            System.exit(1);
        }
        int totalCount = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        DataGeneratorHttpClient client = new DataGeneratorHttpClient();

        long start = System.currentTimeMillis();
        if (threads <= 1) {
            client.generateSequential(totalCount);
        } else {
            client.generateConcurrent(totalCount, threads);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("Inserted %,d records using %d threads in %d ms%n", totalCount, threads, duration);
    }
}
