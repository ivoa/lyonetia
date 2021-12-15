package cds.adql.validation;

import cds.adql.validation.jcommander.CustomUsageFormatter;
import cds.adql.validation.report.MarkdownReport;
import cds.adql.validation.report.StatCollector;
import cds.adql.validation.report.TextReport;
import cds.adql.validation.report.ValidatorListener;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class running the command line version of the ADQL Validator.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (12/2021)
 */
public class ADQLValidatorRunner {

    @Parameter(required = true,
               description="(FILE|DIRECTORY)...")
    private List<String> files = new ArrayList<>(5);

    @Parameter(names={"--recursive", "-r"}, description="Browse directories recursively.")
    private boolean recursive = false;

    @Parameter(help=true,
               names={"--help", "-h"},
               description="Display this help.")
    private boolean help = false;

    @Parameter(names={"--quiet", "-q"},
               description="Nothing displayed. Use the exit status code to get the global validation result.")
    private boolean quiet = false;

    @Parameter(names="--show-all",
               description="Display successful and failed tests.")
    private boolean showAll = false;

    @Parameter(names="--no-stats",
               description="Hide the statistics of the whole validation session.")
    private boolean noStats = false;

    @Parameter(names={"--format","-f"},
               description="Report's output format. Supported values: txt (or text), md (or markdown).")
    private OutputFormat format = OutputFormat.TEXT;

    private enum OutputFormat {
        TXT,
        TEXT,
        MD,
        MARKDOWN;
    }

    public static void main(final String[] args)
    {
        // Create the runner:
        final ADQLValidatorRunner main = new ADQLValidatorRunner();

        // Create and configure the argument parser:
        JCommander commander = JCommander.newBuilder()
                                         .addObject(main)
                                         .build();
        commander.setUsageFormatter(new CustomUsageFormatter(commander));
        commander.setProgramName("java -jar adqlvalidator.jar");

        // Parse the program's arguments:
        commander.parse(args);

        // Run the validator with these arguments:
        main.run(commander);
    }

    /**
     * Run the ADQL Validator with the parsed arguments.
     *
     * <p><i><b>Note:</b>
     *  All parsed arguments are stored in this instance of
     *  {@link ADQLValidatorRunner}. The given parameter - commander - is the
     *  tool used to parse the arguments. It is useful here only to get the
     *  help/usage of this command line program.
     * </i></p>
     *
     * @param commander Tool used to parse arguments.
     */
    protected void run(final JCommander commander){
        // USAGE
        if (help) {
            commander.usage();
            System.exit(0);
        }

        // Configure the validator:
        ADQLValidator validator = new ADQLValidator();

        // Add a statistics collector, if asked for:
        StatCollector statCollector = null;
        if (!noStats) {
            statCollector = new StatCollector();
            validator.addListener(statCollector);
        }

        // Append the result reporter, if not quiet:
        if (!quiet) {
            // ...create the reporter:
            final ValidatorListener reporter;
            switch(format){
                case MD:
                case MARKDOWN:
                    reporter = new MarkdownReport();
                    break;
                case TXT:
                case TEXT:
                default:
                    reporter = new TextReport();
                    break;
            }
            // ...filter its output:
            reporter.setShowOnlyFailures(!showAll);
            // ...associate the reporter to  the stats collector, if any:
            reporter.setValidationStats(statCollector);
            // ...give this reporter to the validator:
            validator.addListener(reporter);
        }

        // Validate each listed file/directory:
        boolean allValid = true;
        for(String filePath : files)
            allValid = validate(new File(filePath), validator) && allValid;

        System.exit(allValid ? 0 : 1);
    }

    protected boolean validate(final File file, final ADQLValidator validator){
        if (file.isDirectory())
        {
            boolean allValid = true;
            for (File f : file.listFiles()) {
                if (!f.isDirectory() || recursive)
                    allValid = validate(f, validator) && allValid;
            }
            return allValid;
        }
        else
            return validateFile(file, validator);
    }

    protected boolean validateFile(final File file, final ADQLValidator validator){
        // Forget about any previous collected statistics:
        final Iterator<ValidatorListener> itListener = validator.getListeners();
        while(itListener.hasNext()){
            itListener.next().clear();
        }

        try{
            // Check the input document:
            try(InputStream stream = new FileInputStream(file)) {
                validator.checkXML(stream);
            }

            // Parse and validate the validation queries:
            try(InputStream stream = new FileInputStream(file)) {
                // ...and validate the tests set:
                return validator.validateXML(stream, "File (" + file.getAbsolutePath() + ")");
            }
        }
        catch(Exception ex){
            validator.publishError(new ValidationException("Failed to open the file '"+file.getAbsolutePath()+"'! Cause: "+ex.getMessage(),ex));
            return false;
        }
    }

}
