package com.mycompany.honorofkingssystem;

// 专门负责「排版」的小工具 class，不含任何跟人物/关系有关的逻辑。
// 刻意只用纯 ASCII 字元 (=、-、空白)，不用颜色也不用特殊 unicode 符号，这样不管在 NetBeans 的 Output console 还是一般 terminal/cmd都不会乱码。
 

public final class ConsoleUI {

    // 每一条线、每一个标题框，统一用这个宽度 (字元数)
    public static final int WIDTH = 64;

    private ConsoleUI() {
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    // Prints一条粗线，长度固定是 WIDTH。
    public static void printHeavyLine() {
        System.out.println(repeat('=', WIDTH));
    }

    // Print一条细线，长度固定是 WIDTH。
    public static void printLightLine() {
        System.out.println(repeat('-', WIDTH));
    }

     /*
     * 把 text 置中print出来，两边自动补空白，补到总长度等于 WIDTH。
     * 如果 text 本身比 WIDTH 还长，就不补空白直接印。
     * Eg.
     *   printCentered("MAIN MENU");
     *   → "                           MAIN MENU"（自动置中）
     */
    public static void printCentered(String text) {
        int padding = WIDTH - text.length();
        int left = Math.max(padding / 2, 0);
        int right = Math.max(padding - left, 0);
        System.out.println(repeat(' ', left) + text + repeat(' ', right));
    }

     /*
     * Print一个「 」的粗框
     * 用在只有一个标题、下面直接接选单/输入的地方 (例如每个菜单画面)。
     * Eg. printBanner("ADD CHARACTER") 会印出：
     *   ================================================================
     *                              ADD CHARACTER
     *   ================================================================
     */
    public static void printBanner(String title) {
        printHeavyLine();
        printCentered(title);
        printHeavyLine();
    }

   
    public static void printHeader(String title) {
        printHeavyLine();
        printCentered(title);
        printLightLine();
    }

    /** Closes a report block that was opened with {@link #printHeader(String)}. */
    public static void printFooter() {
        printHeavyLine();
    }
}