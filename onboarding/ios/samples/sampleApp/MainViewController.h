/******************************************************************************
 *  * 
 *    Copyright (c) 2016 Open Connectivity Foundation and AllJoyn Open
 *    Source Project Contributors and others.
 *    
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0

 ******************************************************************************/

#import <UIKit/UIKit.h>
#import "AJNBusListener.h"
#import "OnboardingStartedListener.h"
#import "AJNAboutListener.h"
#import "AJNSessionListener.h"

@interface MainViewController : UIViewController <AJNBusListener, AJNAboutListener, UITableViewDataSource, UITableViewDelegate, OnboardingStartedListener, AJNSessionListener>

@property (weak, nonatomic) IBOutlet UIButton *connectButton;
@property (weak, nonatomic) IBOutlet UITableView *servicesTable;

- (IBAction)connectButtonDidTouchUpInside:(id)sender;
@property (weak, nonatomic) IBOutlet UILabel *instructionsLabel;

@end