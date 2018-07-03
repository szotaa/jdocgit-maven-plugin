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

    private void pushToGithub() throws IOException, InterruptedException {
        String projectRootPath = System.getProperty("user.dir");
        ProcessBuilder pb = new ProcessBuilder("git", "add", getPath("docs"));
        pb.directory(new File(projectRootPath));
        Process p1 = pb.start();
        printResponse(p1.getErrorStream());
        printResponse(p1.getInputStream());
        p1.waitFor(getTimeout(), TimeUnit.SECONDS);
        pb = new ProcessBuilder("git", "commit", "-m", commitMessage);
        Process p2 = pb.start();
        printResponse(p2.getErrorStream());
        printResponse(p2.getInputStream());
        p2.waitFor(getTimeout(), TimeUnit.SECONDS);
        pb = new ProcessBuilder("git", "push");
        Process p3 = pb.start();
        printResponse(p3.getErrorStream());
        printResponse(p3.getInputStream());
        p3.waitFor(getTimeout(), TimeUnit.SECONDS);
    }

    private void generateJavaDocs() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "javadoc",
                "-d", getPath("docs"),
                "-sourcepath", getPath("src", "main", "java"),
                "-subpackages", subpackages
        );
        Process p = pb.start();
        printResponse(p.getErrorStream());
        printResponse(p.getInputStream());
        p.waitFor(getTimeout(), TimeUnit.SECONDS);
    }

    private String getPath(String... folders){
        String projectRootPath = System.getProperty("user.dir") + File.separator;
        StringJoiner stringJoiner = new StringJoiner(File.separator, projectRootPath, "");
        Arrays.stream(folders).forEach(stringJoiner::add);
        System.out.print(stringJoiner.toString());
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
