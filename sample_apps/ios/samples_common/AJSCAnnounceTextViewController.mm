/******************************************************************************
 * Copyright (c) 2016 Open Connectivity Foundation (OCF) and AllJoyn Open
 *    Source Project (AJOSP) Contributors and others.
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Copyright 2016 Open Connectivity Foundation and Contributors to
 *    AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for
 *    any purpose with or without fee is hereby granted, provided that the
 *    above copyright notice and this permission notice appear in all
 *    copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *     DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *     PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *     TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *     PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

#import "AJSCAnnounceTextViewController.h"
#import "AJSCAboutDataConverter.h"

@interface AJSCAnnounceTextViewController ()
@property (weak, nonatomic) IBOutlet UITextView *announceInformation;

@end

@implementation AJSCAnnounceTextViewController

- (NSString *)objectDescriptionsToString:(NSMutableDictionary *)qnsObjectDesc
{
    NSMutableString *qnsObjectDescContent = [[NSMutableString alloc] init];

    for (NSString *key in qnsObjectDesc.allKeys) {
        //  iterate over the NSMutableDictionary
        //  path: <key>
        [qnsObjectDescContent appendString:[NSString stringWithFormat:@"path: %@ \n", key]];
        [qnsObjectDescContent appendString:[NSString stringWithFormat:@"interfaces: "]];
        //  interfaces: <NSMutableArray of NSString with ' ' between each element>
        for (NSString *intrf in qnsObjectDesc[key]) {
            //  get NSString from the received object(NSMutableArray)
            [qnsObjectDescContent appendString:[NSString stringWithFormat:@"%@ ", intrf]];
        }
        [qnsObjectDescContent appendString:[NSString stringWithFormat:@"\n\n"]];
    }

    return qnsObjectDescContent;
} //  parseObjectDescriptions

- (void)viewDidLoad
{
    [super viewDidLoad];

    //  retrive AJSCAboutAnnouncement by the announcementButtonCurrentTitle unique name
    NSString *txt = [[NSString alloc] init];

    //  set title
    NSString *title = [self.announcement busName];

    txt = [txt stringByAppendingFormat:@"%@\n%@\n", title, [@""stringByPaddingToLength:[title length] + 10 withString:@"-" startingAtIndex:0]];

    //  set body
    txt = [txt stringByAppendingFormat:@"BusName: %@\n", [self.announcement busName]];

    txt = [txt stringByAppendingFormat:@"Port: %hu\n", [self.announcement port]];

    txt = [txt stringByAppendingFormat:@"Version: %u\n", [self.announcement version]];

    txt = [txt stringByAppendingString:@"\n\n"];

    //  set AboutMap info
    txt = [txt stringByAppendingFormat:@"About map:\n"];

    txt = [txt stringByAppendingString:[AJSCAboutDataConverter aboutDataArgString:([self.announcement aboutDataArg])]];

    txt = [txt stringByAppendingString:@"\n\n"];

    //  set ObjectDesc info
    txt = [txt stringByAppendingFormat:@"Bus Object Description:\n"];

    txt = [txt stringByAppendingString:[AJSCAboutDataConverter objectDescriptionArgString:([self.announcement objectDescriptionArg])]];

    self.announceInformation.text = txt;
}


@end