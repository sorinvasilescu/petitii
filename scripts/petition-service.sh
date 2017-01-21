#!/usr/bin/env bash

scriptPath=$(dirname "$(readlink -f "$0")")

# customizable parameters
name="petitii"
profile="development"
config="src/main/resources/config/application.yml"
log_path="."
jar_name="target/petitii-*.jar"

# static parameters
dir="${scriptPath}/../"
pid_file="${log_path}/${name}.pid"
stdout_log="${log_path}/server.log"
stderr_log="${log_path}/server.err"

get_pid() {
    cat "${pid_file}"
}

is_running() {
    [ -f "${pid_file}" ] && ps `get_pid` > /dev/null 2>&1
}

case "$1" in
    start)
    if is_running; then
        echo "Service already started ..."
    else
        # check permissions
        echo "::: Checking permissions for ${name}"
        echo "::: Active profile is ${profile}"

        if [ -w ${log_path} ] ; then
            echo "[x] Service path (${log_path}) is writable."
        else
            echo "[-] Service path (${log_path}) is not writable. Exiting ..."
            exit 1;
        fi

        if [ -r ${config} ] ; then
            echo "[x] Config file (${config}) is readable."
        else
            echo "[-] Config file (${config}) is not readable. Exiting ..."
            exit 1;
        fi

        if [ -r ${jar_name} ] ; then
            echo "[x] Jar file (${jar_name}) is readable."
        else
            echo "[-] Jar file (${jar_name})  is not readable. Exiting ..."
            exit 1;
        fi
        echo "::: Service is ready to be started, switching to ${dir} folder ..."

        cd "$dir"
        nohup java -Dspring.profiles.active=${profile} -Dspring.config.location=file:${config} -jar ${jar_name} > ${stdout_log} 2> ${stderr_log} &
        echo $! > "${pid_file}"
        if ! is_running; then
            echo "[-] Unable to start, see ${stdout_log} and ${stderr_log}"
            exit 1
        fi
        echo "::: Service ${name} started. Enjoy ..."
    fi
    ;;
    stop)
    if is_running; then
        echo -n "Stopping $name.."
        kill `get_pid`
        for i in {1..10}; do
            if ! is_running; then
                break
            fi

            echo -n "."
            sleep 1
        done
        echo

        if is_running; then
            echo "Not stopped; may still be shutting down or shutdown may have failed"
            exit 1
        else
            echo "Stopped"
            if [ -f "${pid_file}" ]; then
                rm "${pid_file}"
            fi
        fi
    else
        echo "Not running"
    fi
    ;;
    restart)
    $0 stop
    if is_running; then
        echo "Unable to stop, will not attempt to start"
        exit 1
    fi
    $0 start
    ;;
    status)
    if is_running; then
        echo "Running"
    else
        echo "Stopped"
        exit 1
    fi
    ;;
    *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0
