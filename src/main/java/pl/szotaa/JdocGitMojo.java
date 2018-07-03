package pl.szotaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven Plugin MOJO which generates JavaDocs and pushes them to GitHub Pages. Plugin assumes you
 * enabled building GitHub Pages from /docs folder on master branch, your local git app is currently
 * checked out at branch master and pushing won't prompt you for credentials.
 * Created as a "hello world" Maven plugin.
 *
 * @author szotaa
 */

@Mojo(name = "JdocGit", defaultPhase = LifecyclePhase.DEPLOY)
public class JdocGitMojo extends AbstractMojo {

    /**
     * Specifies which package should be javadoc'ed. Default value = 'com'.
     */

    @Parameter(property = "subpackages", defaultValue = "com")
    private String subpackages;

    /**
     * Specifies commit message which will be attached to java doc updating commit. Default = 'Update javadoc'
     */

    @Parameter(property = "commitMessage", defaultValue = "Update javadoc")
    private String commitMessage;

    /**
     * Specifies time after which plugin assumes it failed. Default is 10, use larger values if you internet connection is slow
     * or generated javadoc is huge.
     */

    @Parameter(property = "timeout", defaultValue = "10")
    private String timeout;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            generateJavaDocs();
            pushToGithub();

        } catch (IOException | InterruptedException e) {
            throw new MojoFailureException(e.getMessage());
        }

    }

    /**
     * Generates Javadocs using following command: javadoc -d docs -sourcepath /pathToYourRepo/src/main/java -subpackages {subpackages variable}
     */

    private void generateJavaDocs() throws IOException, InterruptedException, MojoFailureException {
        String projectRootPath = System.getProperty("user.dir");
        handleProcess(projectRootPath,
                "javadoc", "-d", getPath("docs"),
                "-sourcepath", getPath("src", "main", "java"),
                "-subpackages", subpackages);
    }

    /**
     * Git adds docs folder, commits with message specified in {commitMessage variable} and pushes to remote repository.
     */

    private void pushToGithub() throws IOException, InterruptedException, MojoFailureException {
        String projectRootPath = System.getProperty("user.dir");
        handleProcess(projectRootPath, "git", "add", getPath("docs"));
        handleProcess(projectRootPath, "git", "commit", "-m", commitMessage);
        handleProcess(projectRootPath, "git", "push");
    }

    /**
     * Helper method which runs specified system commands, prints console output and handles timeouts.
     *
     * @param directory Directory of command execution.
     * @param commands Array of commands and args.
     */

    private void handleProcess(String directory, String... commands) throws IOException, InterruptedException, MojoFailureException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(directory));
        Process p = pb.start();
        printResponse(p.getErrorStream());
        printResponse(p.getInputStream());
        if(!p.waitFor(getTimeout(), TimeUnit.SECONDS)){
            throw new MojoFailureException("process timed out");
        }
    }

    /**
     * Helper method which gets absolute path to your project and appends subfolders specified in folders array.
     * Uses correct file separators for multiple OSes.
     *
     * @param folders Subfolders appended to absolute project path.
     * @return Correctly formatted file path.
     */

    private String getPath(String... folders){
        String projectRootPath = System.getProperty("user.dir") + File.separator;
        StringJoiner stringJoiner = new StringJoiner(File.separator, projectRootPath, "");
        Arrays.stream(folders).forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    /**
     * Helper method which prints process response as Maven info.
     */

    private void printResponse(InputStream stream){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while((line = br.readLine()) != null){
                getLog().info(line);
            }
        } catch (IOException e){
            getLog().info(e.getMessage());
        }
    }

    /**
     * Helper method converting timeout variable to an int.
     */

    private int getTimeout(){
        return Integer.parseInt(timeout);
    }
}
