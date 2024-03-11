function CurentTime()
{
    var now = new Date();

    var year = now.getFullYear();
    var month = now.getMonth() + 1;
    var day = now.getDate();

    var hh = now.getHours();
    var mm = now.getMinutes();
    var ss = now.getSeconds();

    var clock = year + "-";

    if(month < 10)
        clock += "0";

    clock += month + "-";

    if(day < 10)
        clock += "0";

    clock += day + " ";

    if(hh < 10)
        clock += "0";

    clock += hh + ":";
    if (mm < 10) clock += '0';
    clock += mm + ":";

    if (ss < 10) clock += '0';
    clock += ss;
    return(clock);
}

function replaceAll(str1,str2)
{
    while(str1.indexOf(str2) !== -1)
    {
        str1 = str1.replace(str2,"");
    }
    return str1;
}

function loadContent() {
    content = document.getElementById("existFileContent").innerHTML;
    quill.setText(content);
    quill.setSelection(0);
    content = document.getElementById('editor-container').children[0].innerHTML;
}

function update(diff) {
    
    var linecount = 0;
    var position = 0;
    var result = '';
    for(var i = 0; i < diff.length; i++)
    {
        var difflist = Array.from(diff[i]);
        var difftype = difflist[0];
        var diffstring = difflist[1];
        var re = new RegExp("<p>","g");
        var re2 = new RegExp("</p>","g");
        var startcount = 0;
        var endcount = 0;
        if(diffstring.match(re) != null)
        {
            startcount = diffstring.match(re).length;
        }
        if(diffstring.match(re2) != null)
        {
            endcount = diffstring.match(re2).length;
        }
        if(diffstring.replace('\n','') !== '')
        {
            if(difftype === 1)
            {
                if(startcount === 0)
                {
                    if(endcount === 0)
                    {
                        var beforedifflist = Array.from(diff[i-1]);
                        var nextdifflist = Array.from(diff[i+1]);
                        var beforediffstring = beforedifflist[1];
                        var nextdiffstring = nextdifflist[1];
                        var beforedifftype = beforedifflist[0];
                        var nextdifftype = nextdifflist[0];
                        result = result + "ins," + linecount.toString() + "," + position.toString() + "," + diffstring.toString() + ",false.";
                    }
                } else
                {
                    if(diffstring.indexOf("<p>") === 0 && diffstring.lastIndexOf("</p>") === diffstring.length - 4)
                    {
                        diffstring = replaceAll(diffstring,"<p>");
                        var textlist = diffstring.split("</p>");
                        textlist.splice(textlist.length-1,1);
                        for(var j = 0; j < textlist.length; j++)
                        {
                            result = result + "ins," + linecount.toString() + "," + position.toString() + "," + textlist[j].toString() + ",true.";
                        }
                    }
                    if(diffstring.indexOf("</p><p>") !== -1)
                    {
                        var beforedifflist = Array.from(diff[i-1]);
                        var nextdifflist = Array.from(diff[i+1]);
                        var beforediffstring = beforedifflist[1];
                        var nextdiffstring = nextdifflist[1];
                        var beforedifftype = beforedifflist[0];
                        var nextdifftype = nextdifflist[0];
                        if(diffstring === '</p><p>')
                        {
                            if(beforedifftype === 0 && beforediffstring.lastIndexOf("<p>") === beforediffstring.length - 3)
                            {
                                result = result + "ins," + linecount.toString() + "," + position.toString() + "," + "" + ",true.";
                            } else
                            {
                                //console.log(nextdiffstring.indexOf("</p>"));
                                var alterstring = nextdiffstring.slice(0,nextdiffstring.indexOf("</p>"));
                                //console.log(alterstring);
                                result = result + "del," + linecount.toString() + "," + position.toString() + "," + alterstring.toString() + ",false.";
                                position = 0;
                                result = result + "ins," + linecount.toString() + "," + position.toString() + "," + alterstring.toString() + ",true.";
                            }
                        } else
                        {
                            result = result + "ins," + linecount.toString() + "," + position.toString() + "," + diffstring.replace('</p><p>','') + ",true.";
                        }
                        
                    }
                    position = 0;
                }
            } else if (difftype === 0)
            {
                if(startcount === 0)
                {
                    if(endcount === 0)
                    {
                        position += diffstring.length;
                    }
                } else
                {
                    if(endcount === 0)
                    {
                        var textlist = diffstring.split("<p>");
                        linecount += 1;
                        position = textlist[1].length;
                    } else
                    {
                        linecount += startcount;
                        if(diffstring.lastIndexOf("</p>") !== diffstring.length - 4)
                        {
                            if(diffstring.lastIndexOf("<p>") === diffstring.length - 3 && i !== diff.length - 1)
                            {
                                if(Array.from(diff[i+1])[0] === 1 && Array.from(diff[i+1])[1].indexOf('</p><p>') !== -1)
                                {
                                    console.log("-1");
                                    linecount -= 1;
                                }
                            }
                            diffstring = replaceAll(diffstring,"<p>");
                            var textlist = diffstring.split("</p>");
                            position = textlist[textlist.length-1].length;
                        }
                    }
                }
            } else if(difftype === -1)
            {
                if(startcount === 0 && endcount === 0)
                {
                    result = result + "del," + linecount.toString() + "," + position.toString() + "," + diffstring.toString() + ",false.";
                }
                else
                {
                    if(diffstring.indexOf("<p>") === 0 && diffstring.lastIndexOf("</p>") === diffstring.length - 4)
                    {
                        if(startcount === 1 && endcount === 1)
                        {
                            linecount += 1;
                            result = result + "del," + linecount.toString() + "," + position.toString() + "," + "" + ",true.";
                        } else
                        {
                            diffstring = replaceAll(diffstring,"<p>");
                            var textlist = diffstring.split("</p>");
                            textlist.splice(textlist.length-1,1);
                            for(var j = 0; j < textlist.length; j++)
                            {
                                linecount += 1;
                                result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[j].toString() + ",true.";
                            }
                        }
                    } else
                    {
                        var textlist = diffstring.split("</p><p>");
                        var beforedifflist = Array.from(diff[i-1]);
                        var nextdifflist = Array.from(diff[i+1]);
                        var beforediffstring = beforedifflist[1];
                        var nextdiffstring = nextdifflist[1];
                        var beforedifftype = beforedifflist[0];
                        var nextdifftype = nextdifflist[0];
                        if(beforedifftype === 0 && nextdifftype === 0)
                        {
                            if(diffstring === "</p><p>")
                            {
                                linecount += 1;
                                result = result + "del," + linecount.toString() + "," + "0" + "," + nextdiffstring.slice(0,nextdiffstring.indexOf("</p>")) + ",true.";
                                result = result + "ins," + (linecount-1).toString() + "," + position.toString() + "," + nextdiffstring.slice(0,nextdiffstring.indexOf("</p>")) + ",false.";
                                position = 0;
                            } else
                            {
                                if(beforediffstring.lastIndexOf("<p>") === beforediffstring.length - 3 && nextdiffstring.indexOf("</p>") === 0)
                                {
                                    for(var j = 0; j < textlist.length - 1; j++)
                                    {
                                        result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[j].toString() + ",true.";
                                        linecount += 1;
                                    }
                                } else
                                {
                                    var isFirstLineDel = true;
                                    var firstdelline = linecount;
                                    var appendposition = position;
                                    if(beforediffstring.lastIndexOf("<p>") === beforediffstring.length - 3)
                                    {
                                        result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[0].toString() + ",true.";
                                    } else
                                    {
                                        result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[0].toString() + ",false.";
                                        position = 0;
                                        isFirstLineDel = false;
                                    }
                                    for(var j = 1; j < textlist.length - 1; j++)
                                    {
                                        linecount += 1;
                                        result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[j].toString() + ",true.";
                                    }
                                    linecount += 1;
                                    if(nextdiffstring.indexOf("</p>") === 0)
                                    {
                                        result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[textlist.length-1].toString() + ",true.";
                                    } else
                                    {
                                        if(isFirstLineDel)
                                        {
                                            result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[textlist.length-1].toString() + ",false.";
                                        } else
                                        {
                                            var appendstring = nextdiffstring.slice(0,nextdiffstring.indexOf("</p>"));
                                            result = result + "del," + linecount.toString() + "," + position.toString() + "," + textlist[textlist.length-1].toString() + appendstring + ",true.";
                                            result = result + "ins," + firstdelline.toString() + "," + appendposition.toString() + "," + appendstring.toString() + ",false.";
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    return result

}

export {CurentTime,replaceAll,loadContent,update}