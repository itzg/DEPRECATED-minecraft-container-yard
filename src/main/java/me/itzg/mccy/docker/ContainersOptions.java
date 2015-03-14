package me.itzg.mccy.docker;

/**
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public class ContainersOptions {

    private boolean showAll;
    private Integer limit;
    private String sinceId;
    private String beforeId;
    private boolean includeSize;
    private String filters;
    private Integer withExitCode;
    private ContainerStatus withStatus;

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSinceId() {
        return sinceId;
    }

    public void setSinceId(String sinceId) {
        this.sinceId = sinceId;
    }

    public String getBeforeId() {
        return beforeId;
    }

    public void setBeforeId(String beforeId) {
        this.beforeId = beforeId;
    }

    public boolean isIncludeSize() {
        return includeSize;
    }

    public void setIncludeSize(boolean includeSize) {
        this.includeSize = includeSize;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Integer getWithExitCode() {
        return withExitCode;
    }

    public void setWithExitCode(int withExitCode) {
        this.withExitCode = withExitCode;
    }

    public ContainerStatus getWithStatus() {
        return withStatus;
    }

    public void setWithStatus(ContainerStatus withStatus) {
        this.withStatus = withStatus;
    }
}
