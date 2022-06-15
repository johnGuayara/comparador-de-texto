import org.apache.commons.io.FileUtils;
import org.apache.commons.text.diff.CommandVisitor;
import org.apache.commons.io.LineIterator;
import org.apache.commons.text.diff.StringsComparator;

import java.io.File;
import java.io.IOException;

public class CompararEtiquetas {
    public static void main(String[] args) throws IOException {
        LineIterator file1 = FileUtils.lineIterator(new File("src/file-1.txt"), "utf-8");
        LineIterator file2 = FileUtils.lineIterator(new File("src/file-2.txt"), "utf-8");

        FileCommandsVisitor fileCommandsVisitor = new FileCommandsVisitor();

        while (file1.hasNext() || file2.hasNext()){
            String left = (file1.hasNext() ? file1.nextLine() : "") + "\n";
            String right = (file2.hasNext() ? file2.nextLine(): "") + "\n";
             StringsComparator comparator = new StringsComparator(left, right);

             if (comparator.getScript().getLCSLength() > (Integer.max( left.length(), right.length())* 0.4)) {
                comparator.getScript().visit(fileCommandsVisitor);
             } else{
                 StringsComparator leftComparator = new StringsComparator(left, "\n");
                 leftComparator.getScript().visit(fileCommandsVisitor);
                 StringsComparator rightComparator = new StringsComparator("\n", right);
                 rightComparator.getScript().visit(fileCommandsVisitor);
             }
        }
        fileCommandsVisitor.generateHTML();
    }
}
    class FileCommandsVisitor implements CommandVisitor<Character>{
        private static final String DELETION =  "<span style=\"background-color: #FB504B\">${text}</span>";
        private static final String INSERTION = "<span style=\"background-color: #45EA85\">${text}</span>";

        private String left = "";
        private String right = "";


        @Override
        public void visitInsertCommand(Character c) {
            String toAppend = "\n".equals("" + c) ? "<br/>" : "" + c;
            right = right + INSERTION.replace("${text}", "" + toAppend);
        }

        @Override
        public void visitKeepCommand(Character c) {
            String toAppend = "\n".equals(""+c) ? "<br/>" : "" + c;

            left = left + toAppend;
            right = right + toAppend;
        }

        @Override
        public void visitDeleteCommand(Character c) {
            String toAppend = "\n".equals("" + c) ? "<br/>" : "" + c;
            left = left + DELETION.replace("${text}", "" + toAppend);
        }

        public void generateHTML() throws IOException {
            String template = FileUtils.readFileToString(new File("src/difftemplate.html"), "utf-8");
            String out1 =template.replace("${left}", left);
            String output = out1.replace("${right}", right);

            FileUtils.write(new File("finalDiff.html"), output, "utf-8");
            System.out.println("HTML diff generated.");
        }
    }
