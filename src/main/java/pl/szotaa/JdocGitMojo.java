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

@Mojo(name = "JdocGit", defaultPhase = LifecyclePhase.DEPLOY)
public class JdocGitMojo extends AbstractMojo {

    @Parameter(property = "subpackages", defaultValue = "com")
    private String subpackages;

    @Parameter(property = "remoteName", defaultValue = "remote")
    private String remoteName;

    @Parameter(property = "commitMessage", defaultValue = "Update javadoc")
    private String commitMessage;

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

    private void pushToGithub() throws IOException, InterruptedException, MojoFailureException {
        String projectRootPath = System.getProperty("user.dir");
        handleProcess(projectRootPath, "git", "add", getPath("docs"));
        handleProcess(projectRootPath, "git", "commit", "-m", commitMessage);
        handleProcess(projectRootPath, "git", "push");
    }

    private void generateJavaDocs() throws IOException, InterruptedException, MojoFailureException {
        String projectRootPath = System.getProperty("user.dir");
        handleProcess(projectRootPath,
                "javadoc", "-d", getPath("docs"),
                "-sourcepath", getPath("src", "main", "java"),
                "-subpackages", subpackages);
    }

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

    private String getPath(String... folders){
        String projectRootPath = System.getProperty("user.dir") + File.separator;
        StringJoiner stringJoiner = new StringJoiner(File.separator, projectRootPath, "");
        Arrays.stream(folders).forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

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

    private int getTimeout(){
        return Integer.parseInt(timeout);
    }
}
