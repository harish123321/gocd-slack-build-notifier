package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class GoNotificationMessageTest_FixStageResult {

    public static final String PIPELINE_NAME = "PL";
    public static final String STAGE_NAME = "STG";

    private History pipelineHistory;
    private GoNotificationMessage.PipelineInfo pipeline;
    private String expectedStatus;

    public GoNotificationMessageTest_FixStageResult(History pipelineHistory, GoNotificationMessage.PipelineInfo pipeline, String expectedStatus) {
        this.pipelineHistory = pipelineHistory;
        this.pipeline = pipeline;
        this.expectedStatus = expectedStatus;
    }

    @Parameterized.Parameters(name = "Test #{index}: Pipeline {1} should return correct status {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "building"))),
                thenExpectStatus("building")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), "Failed"),
                        stage(STAGE_NAME,           counter(1), "Failed"),
                        stage("other-stage-name-2", counter(1), "Failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), "Passed"),
                        stage(STAGE_NAME,           counter(1), "Failed"),
                        stage("other-stage-name-2", counter(1), "Passed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "Failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "Failed"))),
                thenExpectStatus("Broken")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "Failed"))),
                thenExpectStatus("Broken")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "passed"))),
                thenExpectStatus("passed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "failed"))),
                thenExpectStatus("failed")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "cancelled"))),
                thenExpectStatus("cancelled")
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "cancelled"))),
                thenExpectStatus("cancelled")
        }
        });
    }

    @Test
    public void test() throws IOException {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                pipeline
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is(expectedStatus));
    }

    private static History givenHistory(Pipeline... pipelines) {
        History history = new History();
        history.pipelines = pipelines;
        return history;
    }

    private static Pipeline pipeline(String name, int counter, Stage... stages) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        pipeline.stages = stages;

        return pipeline;
    }

    private static Stage stage(String name, int counter, String status) {
        Stage stage = new Stage();
        stage.name = name;
        stage.counter = counter;
        stage.result = status;
        return stage;
    }

    private static GoNotificationMessage.PipelineInfo whenPipelineFinished(Pipeline pipeline) {
        GoNotificationMessage.PipelineInfo info = new GoNotificationMessage.PipelineInfo();
        info.name = pipeline.name;
        info.counter = Integer.toString(pipeline.counter);
        info.stage = new GoNotificationMessage.StageInfo();

        Stage stage = pipeline.stages[0];
        info.stage.counter = Integer.toString(stage.counter);
        info.stage.name = stage.name;
        info.stage.state = stage.result;
        info.stage.result = stage.result;
        return info;
    }

    private static String thenExpectStatus(String value) {
        return value;
    }

    private static int counter(int value) {
        return value;
    }

}